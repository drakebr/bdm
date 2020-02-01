package com.bdmplatform.consensus

import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.transaction.Transaction

object TransactionsOrdering {
  trait BdmOrdering extends Ordering[Transaction] {
    def txTimestampOrder(ts: Long): Long
    private def orderBy(t: Transaction): (Double, Long, Long) = {
      val size        = t.bytes().length
      val byFee       = if (t.assetFee._1 != Bdm) 0 else -t.assetFee._2
      val byTimestamp = txTimestampOrder(t.timestamp)

      (byFee.toDouble / size.toDouble, byFee, byTimestamp)
    }
    override def compare(first: Transaction, second: Transaction): Int = {
      implicitly[Ordering[(Double, Long, Long)]].compare(orderBy(first), orderBy(second))
    }
  }

  object InBlock extends BdmOrdering {
    // sorting from network start
    override def txTimestampOrder(ts: Long): Long = -ts
  }

  object InUTXPool extends BdmOrdering {
    override def txTimestampOrder(ts: Long): Long = ts
  }
}
