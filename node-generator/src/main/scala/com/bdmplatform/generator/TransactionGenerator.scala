package com.bdmplatform.generator

import com.bdmplatform.transaction.Transaction

trait TransactionGenerator extends Iterator[Iterator[Transaction]] {
  override val hasNext          = true
  def initial: Seq[Transaction] = Seq.empty
}
