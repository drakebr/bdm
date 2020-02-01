package com.bdmplatform

import com.typesafe.config.ConfigFactory
import com.bdmplatform.account.KeyPair
import com.bdmplatform.block.{Block, MicroBlock}
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.consensus.nxt.NxtLikeConsensusBlockData
import com.bdmplatform.crypto._
import com.bdmplatform.features.{BlockchainFeature, BlockchainFeatures}
import com.bdmplatform.lagonaki.mocks.TestBlock
import com.bdmplatform.settings._
import com.bdmplatform.transaction.Transaction

package object history {
  val MaxTransactionsPerBlockDiff = 10
  val MaxBlocksInMemory           = 5
  val DefaultBaseTarget           = 1000L
  val DefaultBlockchainSettings = BlockchainSettings(
    addressSchemeCharacter = 'N',
    functionalitySettings = TestFunctionalitySettings.Enabled,
    genesisSettings = GenesisSettings.TESTNET,
    rewardsSettings = RewardsSettings.TESTNET
  )

  val config   = ConfigFactory.load()
  val settings = BdmSettings.fromRootConfig(config)

  val MicroblocksActivatedAt0BdmSettings: BdmSettings        = settingsWithFeatures(BlockchainFeatures.NG)
  val DataAndMicroblocksActivatedAt0BdmSettings: BdmSettings = settingsWithFeatures(BlockchainFeatures.DataTransaction, BlockchainFeatures.NG)
  val TransfersV2ActivatedAt0BdmSettings: BdmSettings        = settingsWithFeatures(BlockchainFeatures.SmartAccounts)

  def settingsWithFeatures(features: BlockchainFeature*): BdmSettings = {
    val blockchainSettings = DefaultBlockchainSettings.copy(
      functionalitySettings = DefaultBlockchainSettings.functionalitySettings.copy(preActivatedFeatures = features.map(_.id -> 0).toMap)
    )
    settings.copy(blockchainSettings = blockchainSettings)
  }

  val DefaultBdmSettings: BdmSettings = settings.copy(
    blockchainSettings = DefaultBlockchainSettings,
    featuresSettings = settings.featuresSettings.copy(autoShutdownOnUnsupportedFeature = false)
  )

  val defaultSigner       = KeyPair(Array.fill(KeyLength)(0: Byte))
  val generationSignature = ByteStr(Array.fill(Block.GeneratorSignatureLength)(0: Byte))

  def buildBlockOfTxs(refTo: ByteStr, txs: Seq[Transaction]): Block =
    buildBlockOfTxs(refTo, txs, txs.headOption.fold(0L)(_.timestamp))

  def buildBlockOfTxs(refTo: ByteStr, txs: Seq[Transaction], timestamp: Long): Block =
    customBuildBlockOfTxs(refTo, txs, defaultSigner, 1, timestamp)

  def customBuildBlockOfTxs(
      refTo: ByteStr,
      txs: Seq[Transaction],
      signer: KeyPair,
      version: Byte,
      timestamp: Long,
      bTarget: Long = DefaultBaseTarget
  ): Block =
    Block
      .buildAndSign(
        version = version,
        timestamp = timestamp,
        reference = refTo,
        consensusData = NxtLikeConsensusBlockData(baseTarget = bTarget, generationSignature = generationSignature),
        transactionData = txs,
        signer = signer,
        Set.empty,
        -1L
      )
      .explicitGet()

  def customBuildMicroBlockOfTxs(
      totalRefTo: ByteStr,
      prevTotal: Block,
      txs: Seq[Transaction],
      signer: KeyPair,
      version: Byte,
      ts: Long
  ): (Block, MicroBlock) = {
    val newTotalBlock = customBuildBlockOfTxs(totalRefTo, prevTotal.transactionData ++ txs, signer, version, ts)
    val nonSigned = MicroBlock
      .buildAndSign(
        generator = signer,
        transactionData = txs,
        prevResBlockSig = prevTotal.uniqueId,
        totalResBlockSig = newTotalBlock.uniqueId
      )
      .explicitGet()
    (newTotalBlock, nonSigned)
  }

  def buildMicroBlockOfTxs(totalRefTo: ByteStr, prevTotal: Block, txs: Seq[Transaction], signer: KeyPair): (Block, MicroBlock) = {
    val newTotalBlock = buildBlockOfTxs(totalRefTo, prevTotal.transactionData ++ txs)
    val nonSigned = MicroBlock
      .buildAndSign(
        generator = signer,
        transactionData = txs,
        prevResBlockSig = prevTotal.uniqueId,
        totalResBlockSig = newTotalBlock.uniqueId
      )
      .explicitGet()
    (newTotalBlock, nonSigned)
  }

  def randomSig: ByteStr = TestBlock.randomOfLength(Block.BlockIdLength)

  def chainBlocks(txs: Seq[Seq[Transaction]]): Seq[Block] = {
    def chainBlocksR(refTo: ByteStr, txs: Seq[Seq[Transaction]]): Seq[Block] = txs match {
      case (x :: xs) =>
        val block = buildBlockOfTxs(refTo, x)
        block +: chainBlocksR(block.uniqueId, xs)
      case _ => Seq.empty
    }

    chainBlocksR(randomSig, txs)
  }

  def chainBaseAndMicro(totalRefTo: ByteStr, base: Transaction, micros: Seq[Seq[Transaction]]): (Block, Seq[MicroBlock]) =
    chainBaseAndMicro(totalRefTo, Seq(base), micros, defaultSigner, 3, base.timestamp)

  def chainBaseAndMicro(
      totalRefTo: ByteStr,
      base: Seq[Transaction],
      micros: Seq[Seq[Transaction]],
      signer: KeyPair,
      version: Byte,
      timestamp: Long
  ): (Block, Seq[MicroBlock]) = {
    val block = customBuildBlockOfTxs(totalRefTo, base, signer, version, timestamp)
    val microBlocks = micros
      .foldLeft((block, Seq.empty[MicroBlock])) {
        case ((lastTotal, allMicros), txs) =>
          val (newTotal, micro) = customBuildMicroBlockOfTxs(totalRefTo, lastTotal, txs, signer, version, timestamp)
          (newTotal, allMicros :+ micro)
      }
      ._2
    (block, microBlocks)
  }

  def spoilSignature(b: Block): Block = b.copy(signerData = b.signerData.copy(signature = TestBlock.randomSignature()))
}
