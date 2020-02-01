package com.bdmplatform.http
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.network.UtxPoolSynchronizer
import com.bdmplatform.transaction.Transaction
import com.bdmplatform.transaction.smart.script.trace.TracedResult
import io.netty.channel.Channel

object DummyUtxPoolSynchronizer {
  val accepting: UtxPoolSynchronizer = new UtxPoolSynchronizer {
    override def tryPublish(tx: Transaction, source: Channel): Unit               = {}
    override def publish(tx: Transaction): TracedResult[ValidationError, Boolean] = TracedResult(Right(true))
  }

  def rejecting(error: Transaction => ValidationError): UtxPoolSynchronizer = new UtxPoolSynchronizer {
    override def tryPublish(tx: Transaction, source: Channel): Unit               = {}
    override def publish(tx: Transaction): TracedResult[ValidationError, Boolean] = TracedResult(Left(error(tx)))
  }
}
