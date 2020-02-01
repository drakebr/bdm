package com.bdmplatform.utx

import com.bdmplatform.account.Address
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.mining.MultiDimensionalMiningConstraint
import com.bdmplatform.state.Portfolio
import com.bdmplatform.transaction._
import com.bdmplatform.transaction.smart.script.trace.TracedResult

import scala.concurrent.duration.Duration

trait UtxPool extends AutoCloseable {
  def putIfNew(tx: Transaction, verify: Boolean = true): TracedResult[ValidationError, Boolean]

  def removeAll(txs: Traversable[Transaction]): Unit

  def spendableBalance(addr: Address, assetId: Asset): Long

  def pessimisticPortfolio(addr: Address): Portfolio

  def all: Seq[Transaction]

  def size: Int

  def transactionById(transactionId: ByteStr): Option[Transaction]

  def packUnconfirmed(rest: MultiDimensionalMiningConstraint, maxPackTime: Duration): (Option[Seq[Transaction]], MultiDimensionalMiningConstraint)
}
