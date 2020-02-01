package com.bdmplatform.mining.microblocks

import cats.effect.concurrent.Ref
import com.bdmplatform.account.KeyPair
import com.bdmplatform.block.Block
import com.bdmplatform.mining.{MinerDebugInfo, MiningConstraint, MiningConstraints}
import com.bdmplatform.settings.MinerSettings
import com.bdmplatform.state.Blockchain
import com.bdmplatform.transaction.BlockchainUpdater
import com.bdmplatform.utx.UtxPool
import io.netty.channel.group.ChannelGroup
import monix.eval.Task
import monix.execution.schedulers.SchedulerService

import scala.concurrent.duration._

trait MicroBlockMiner {
  def generateMicroBlockSequence(account: KeyPair,
                                 accumulatedBlock: Block,
                                 delay: FiniteDuration,
                                 constraints: MiningConstraints,
                                 restTotalConstraint: MiningConstraint): Task[Unit]
}

object MicroBlockMiner {
  def apply(debugState: Ref[Task, MinerDebugInfo.State],
            allChannels: ChannelGroup,
            blockchainUpdater: BlockchainUpdater with Blockchain,
            utx: UtxPool,
            settings: MinerSettings,
            minerScheduler: SchedulerService,
            appenderScheduler: SchedulerService): MicroBlockMiner =
    new MicroBlockMinerImpl(
      debugState,
      allChannels,
      blockchainUpdater,
      utx,
      settings,
      minerScheduler,
      appenderScheduler,
    )
}
