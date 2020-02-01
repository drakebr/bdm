package com.bdmplatform.mining

import cats.effect.concurrent.Ref
import cats.implicits._
import com.bdmplatform.account.{KeyPair, PublicKey}
import com.bdmplatform.block.Block
import com.bdmplatform.block.Block._
import com.bdmplatform.consensus.PoSSelector
import com.bdmplatform.consensus.nxt.NxtLikeConsensusBlockData
import com.bdmplatform.features.BlockchainFeatures
import com.bdmplatform.features.FeatureProvider._
import com.bdmplatform.metrics.{BlockStats, Instrumented, _}
import com.bdmplatform.mining.microblocks.MicroBlockMiner
import com.bdmplatform.network._
import com.bdmplatform.settings.{FunctionalitySettings, BdmSettings}
import com.bdmplatform.state._
import com.bdmplatform.state.appender.BlockAppender
import com.bdmplatform.transaction._
import com.bdmplatform.utils.{ScorexLogging, Time}
import com.bdmplatform.utx.UtxPoolImpl
import com.bdmplatform.wallet.Wallet
import io.netty.channel.group.ChannelGroup
import kamon.Kamon
import monix.eval.Task
import monix.execution.cancelables.{CompositeCancelable, SerialCancelable}
import monix.execution.schedulers.{CanBlock, SchedulerService}

import scala.concurrent.duration._

trait Miner {
  def scheduleMining(): Unit
}

trait MinerDebugInfo {
  def state: MinerDebugInfo.State
  def getNextBlockGenerationOffset(account: KeyPair): Either[String, FiniteDuration]
}

object MinerDebugInfo {
  sealed trait State
  case object MiningBlocks              extends State
  case object MiningMicroblocks         extends State
  case object Disabled                  extends State
  final case class Error(error: String) extends State
}

class MinerImpl(
    allChannels: ChannelGroup,
    blockchainUpdater: BlockchainUpdater with NG,
    settings: BdmSettings,
    timeService: Time,
    utx: UtxPoolImpl,
    wallet: Wallet,
    pos: PoSSelector,
    val minerScheduler: SchedulerService,
    val appenderScheduler: SchedulerService
) extends Miner
    with MinerDebugInfo
    with ScorexLogging {

  private implicit val s: SchedulerService = minerScheduler

  private lazy val minerSettings              = settings.minerSettings
  private lazy val minMicroBlockDurationMills = minerSettings.minMicroBlockAge.toMillis
  private lazy val blockchainSettings         = settings.blockchainSettings

  private val scheduledAttempts = SerialCancelable()
  private val microBlockAttempt = SerialCancelable()

  private val debugStateRef: Ref[Task, MinerDebugInfo.State] = Ref.unsafe[Task, MinerDebugInfo.State](MinerDebugInfo.Disabled)

  private val microBlockMiner: MicroBlockMiner = MicroBlockMiner(
    debugStateRef,
    allChannels,
    blockchainUpdater,
    utx,
    settings.minerSettings,
    minerScheduler,
    appenderScheduler
  )

  def getNextBlockGenerationOffset(account: KeyPair): Either[String, FiniteDuration] =
    this.nextBlockGenOffsetWithConditions(account)

  private def checkAge(parentHeight: Int, parentTimestamp: Long): Either[String, Unit] =
    Either
      .cond(parentHeight == 1, (), (timeService.correctedTime() - parentTimestamp).millis)
      .left
      .flatMap(
        blockAge =>
          Either.cond(
            blockAge <= minerSettings.intervalAfterLastBlockThenGenerationIsAllowed,
            (),
            s"BlockChain is too old (last block timestamp is $parentTimestamp generated $blockAge ago)"
          )
      )

  private def checkScript(account: KeyPair): Either[String, Unit] = {
    Either.cond(!blockchainUpdater.hasScript(account), (), s"Account(${account.toAddress}) is scripted and therefore not allowed to forge blocks")
  }

  private def ngEnabled: Boolean = blockchainUpdater.featureActivationHeight(BlockchainFeatures.NG.id).exists(blockchainUpdater.height > _ + 1)

  private def generateOneBlockTask(account: KeyPair)(delay: FiniteDuration): Task[Either[String, (MiningConstraints, Block, MiningConstraint)]] = {
    Task {
      forgeBlock(account)
    }.delayExecution(delay)
  }

  private def consensusData(
      height: Int,
      account: KeyPair,
      lastBlock: Block,
      refBlockBT: Long,
      refBlockTS: Long,
      balance: Long,
      currentTime: Long
  ): Either[String, NxtLikeConsensusBlockData] = {
    pos
      .consensusData(
        account.publicKey,
        height,
        blockchainSettings.genesisSettings.averageBlockDelay,
        refBlockBT,
        refBlockTS,
        blockchainUpdater.parentHeader(lastBlock, 2).map(_.timestamp),
        currentTime
      )
      .leftMap(_.toString)
  }

  private def forgeBlock(account: KeyPair): Either[String, (MiningConstraints, Block, MiningConstraint)] = {
    // should take last block right at the time of mining since microblocks might have been added
    val height              = blockchainUpdater.height
    val version             = blockchainUpdater.currentBlockVersion
    val lastBlock           = blockchainUpdater.lastBlock.get
    val referencedBlockInfo = blockchainUpdater.bestLastBlockInfo(System.currentTimeMillis() - minMicroBlockDurationMills).get
    val refBlockBT          = referencedBlockInfo.consensus.baseTarget
    val refBlockTS          = referencedBlockInfo.timestamp
    val refBlockID          = referencedBlockInfo.blockId
    lazy val currentTime    = timeService.correctedTime()
    lazy val blockDelay     = currentTime - lastBlock.timestamp
    lazy val balance        = blockchainUpdater.generatingBalance(account.toAddress, Some(refBlockID))

    metrics.blockBuildTimeStats.measureSuccessful(for {
      _ <- checkQuorumAvailable()
      validBlockDelay <- pos
        .getValidBlockDelay(height, account.publicKey, refBlockBT, balance)
        .leftMap(_.toString)
        .ensure(s"$currentTime: Block delay $blockDelay was NOT less than estimated delay")(_ < blockDelay)
      _ = log.debug(
        s"Forging with ${account.toAddress}, Time $blockDelay > Estimated Time $validBlockDelay, balance $balance, prev block $refBlockID at $height with target $refBlockBT"
      )
      consensusData <- consensusData(height, account, lastBlock, refBlockBT, refBlockTS, balance, currentTime)
      estimators   = MiningConstraints(blockchainUpdater, height, Some(minerSettings))
      mdConstraint = MultiDimensionalMiningConstraint(estimators.total, estimators.keyBlock)
      (maybeUnconfirmed, updatedMdConstraint) = Instrumented.logMeasure(log, "packing unconfirmed transactions for block")(
        utx.packUnconfirmed(mdConstraint, settings.minerSettings.maxPackTime)
      )
      unconfirmed = maybeUnconfirmed.getOrElse(Seq.empty)
      _           = log.debug(s"Adding ${unconfirmed.size} unconfirmed transaction(s) to new block")
      block <- Block
        .buildAndSign(version.toByte, currentTime, refBlockID, consensusData, unconfirmed, account, blockFeatures(version), blockRewardVote(version))
        .leftMap(_.err)
    } yield (estimators, block, updatedMdConstraint.constraints.head))
  }

  private def checkQuorumAvailable(): Either[String, Unit] = {
    val chanCount = allChannels.size()
    Either.cond(chanCount >= minerSettings.quorum, (), s"Quorum not available ($chanCount/${minerSettings.quorum}), not forging block.")
  }

  private def blockFeatures(version: Byte): Set[Short] = {
    if (version <= 2) Set.empty[Short]
    else
      settings.featuresSettings.supported
        .filterNot(blockchainUpdater.approvedFeatures.keySet)
        .filter(BlockchainFeatures.implemented)
        .toSet
  }

  private def blockRewardVote(version: Byte): Long =
    if (version < RewardBlockVersion) -1L
    else settings.rewardsSettings.desired.getOrElse(-1L)

  private def nextBlockGenerationTime(fs: FunctionalitySettings, height: Int, block: Block, account: PublicKey): Either[String, Long] = {
    val balance = blockchainUpdater.generatingBalance(account.toAddress, Some(block.uniqueId))

    if (blockchainUpdater.isMiningAllowed(height, balance)) {
      for {
        expectedTS <- pos
          .getValidBlockDelay(height, account, block.consensusData.baseTarget, balance)
          .map(_ + block.timestamp)
          .leftMap(_.toString)
        result <- Either.cond(
          0 < expectedTS && expectedTS < Long.MaxValue,
          expectedTS,
          s"Invalid next block generation time: $expectedTS"
        )
      } yield result
    } else Left(s"Balance $balance of ${account.stringRepr} is lower than required for generation")
  }

  private def nextBlockGenOffsetWithConditions(account: KeyPair): Either[String, FiniteDuration] = {
    val height    = blockchainUpdater.height
    val lastBlock = blockchainUpdater.lastBlock.get
    for {
      _  <- checkAge(height, blockchainUpdater.lastBlockTimestamp.get) // lastBlock ?
      _  <- checkScript(account)
      ts <- nextBlockGenerationTime(blockchainSettings.functionalitySettings, height, lastBlock, account)
      calculatedOffset = ts - timeService.correctedTime()
      offset           = Math.max(calculatedOffset, minerSettings.minimalBlockGenerationOffset.toMillis).millis

    } yield offset
  }

  private def generateBlockTask(account: KeyPair): Task[Unit] = {
    {
      for {
        offset <- nextBlockGenOffsetWithConditions(account)
        quorumAvailable = checkQuorumAvailable().isRight
      } yield {
        if (quorumAvailable) offset
        else offset.max(settings.minerSettings.noQuorumMiningDelay)
      }
    } match {
      case Right(offset) =>
        log.debug(f"Next attempt for acc=${account.toAddress} in ${offset.toUnit(SECONDS)}%.3f")
        generateOneBlockTask(account)(offset).flatMap {
          case Right((estimators, block, totalConstraint)) =>
            BlockAppender(blockchainUpdater, timeService, utx, pos, appenderScheduler)(block)
              .asyncBoundary(minerScheduler)
              .map {
                case Left(err) => log.warn("Error mining Block: " + err.toString)
                case Right(Some(score)) =>
                  log.debug(s"Forged and applied $block by ${account.stringRepr} with cumulative score $score")
                  BlockStats.mined(block, blockchainUpdater.height)
                  allChannels.broadcast(BlockForged(block))
                  scheduleMining()
                  if (ngEnabled && !totalConstraint.isFull) startMicroBlockMining(account, block, estimators, totalConstraint)
                case Right(None) => log.warn("Newly created block has already been appended, should not happen")
              }

          case Left(err) =>
            log.debug(s"No block generated because $err, retrying")
            generateBlockTask(account)
        }

      case Left(err) =>
        log.debug(s"Not scheduling block mining because $err")
        debugStateRef.set(MinerDebugInfo.Error(err))
    }
  }

  def scheduleMining(): Unit = {
    Miner.blockMiningStarted.increment()
    val nonScriptedAccounts = wallet.privateKeyAccounts.filterNot(blockchainUpdater.hasScript(_))
    scheduledAttempts := CompositeCancelable.fromSet(nonScriptedAccounts.map(generateBlockTask).map(_.runAsyncLogErr).toSet)
    microBlockAttempt := SerialCancelable()

    debugStateRef
      .set(MinerDebugInfo.MiningBlocks)
      .runSyncUnsafe(1.second)(minerScheduler, CanBlock.permit)
  }

  private[this] def startMicroBlockMining(
      account: KeyPair,
      lastBlock: Block,
      constraints: MiningConstraints,
      restTotalConstraint: MiningConstraint
  ): Unit = {
    log.info(s"Start mining microblocks")
    Miner.microMiningStarted.increment()
    microBlockAttempt := microBlockMiner
      .generateMicroBlockSequence(account, lastBlock, Duration.Zero, constraints, restTotalConstraint)
      .runAsyncLogErr
    log.trace(s"MicroBlock mining scheduled for $account")
  }

  override def state: MinerDebugInfo.State = debugStateRef.get.runSyncUnsafe(1.second)(minerScheduler, CanBlock.permit)

  private[this] object metrics {
    val blockBuildTimeStats      = Kamon.timer("miner.pack-and-forge-block-time")
    val microBlockBuildTimeStats = Kamon.timer("miner.forge-microblock-time")
  }
}

object Miner {
  val blockMiningStarted = Kamon.counter("block-mining-started")
  val microMiningStarted = Kamon.counter("micro-mining-started")

  val MaxTransactionsPerMicroblock: Int = 500

  case object Disabled extends Miner with MinerDebugInfo {
    override def scheduleMining(): Unit                                                         = ()
    override def getNextBlockGenerationOffset(account: KeyPair): Either[String, FiniteDuration] = Left("Disabled")
    override val state                                                                          = MinerDebugInfo.Disabled
  }
}
