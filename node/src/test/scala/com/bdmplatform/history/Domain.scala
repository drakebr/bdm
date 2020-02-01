package com.bdmplatform.history

import com.bdmplatform.account.Address
import com.bdmplatform.block.Block
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.database.LevelDBWriter
import com.bdmplatform.state._
import com.bdmplatform.state.extensions.Distributions
import com.bdmplatform.transaction.{BlockchainUpdater, Transaction}
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration.Duration

//noinspection ScalaStyle
case class Domain(blockchainUpdater: BlockchainUpdater with NG, levelDBWriter: LevelDBWriter) {
  def effBalance(a: Address): Long = blockchainUpdater.effectiveBalance(a, 1000)

  def appendBlock(b: Block) = blockchainUpdater.processBlock(b).explicitGet()

  def removeAfter(blockId: ByteStr) = blockchainUpdater.removeAfter(blockId).explicitGet()

  def lastBlockId = blockchainUpdater.lastBlockId.get

  def portfolio(address: Address) = Distributions(blockchainUpdater).portfolio(address)

  def addressTransactions(address: Address): Seq[(Height, Transaction)] =
    blockchainUpdater.addressTransactionsObservable(address, Set.empty).take(128).toListL.runSyncUnsafe(Duration.Inf)

  def carryFee = blockchainUpdater.carryFee
}
