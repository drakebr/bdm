package com.bdmplatform.network

import cats.instances.int._
import com.google.common.cache.CacheBuilder
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.settings.SynchronizationSettings.UtxSynchronizerSettings
import com.bdmplatform.transaction.TxValidationError.GenericError
import com.bdmplatform.transaction.smart.script.trace.TracedResult
import com.bdmplatform.transaction.{LastBlockInfo, Transaction}
import com.bdmplatform.utils.ScorexLogging
import com.bdmplatform.utx.UtxPool
import io.netty.channel.Channel
import io.netty.channel.group.ChannelGroup
import monix.eval.Task
import monix.execution.{AsyncQueue, Scheduler}
import monix.reactive.Observable

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Success

trait UtxPoolSynchronizer {
  def tryPublish(tx: Transaction, source: Channel): Unit
  def publish(tx: Transaction): TracedResult[ValidationError, Boolean]
}

class UtxPoolSynchronizerImpl(
    val settings: UtxSynchronizerSettings,
    putIfNew: Transaction => TracedResult[ValidationError, Boolean],
    broadcast: (Transaction, Option[Channel]) => Unit,
    blockSource: Observable[LastBlockInfo],
    timedScheduler: Scheduler
) extends UtxPoolSynchronizer
    with ScorexLogging
    with AutoCloseable {

  import Scheduler.Implicits.global

  private[this] val queue = AsyncQueue.bounded[(Transaction, Channel)](settings.maxQueueSize)

  private[this] val dummy = new Object()
  private[this] val knownTransactions = CacheBuilder
    .newBuilder()
    .maximumSize(settings.networkTxCacheSize)
    .build[ByteStr, Object]

  private[this] val pollLoopCancelable = Observable
    .repeatEvalF(Task.deferFuture(queue.poll()))
    .observeOn(Scheduler.global)
    .mapParallelUnordered(settings.maxThreads) {
      case (tx, source) =>
        Task
          .deferFuture(validateFuture(tx, allowRebroadcast = false, Some(source)))
          .timeout(5 seconds)
          .onErrorRecover { case err => TracedResult.wrapE(Left(GenericError(err.toString))) }
    }
    .doOnComplete(Task(log.info("UtxPoolSynchronizer stopped")))
    .doOnError(e => Task(log.warn("UtxPoolSynchronizer stopped abnormally", e)))
    .subscribe()

  blockSource.map(_.height).distinctUntilChanged.foreach(_ => knownTransactions.invalidateAll())

  override def tryPublish(tx: Transaction, source: Channel): Unit = {
    def transactionIsNew(txId: ByteStr): Boolean = {
      var isNew = false
      knownTransactions.get(txId, { () =>
        isNew = true; dummy
      })
      isNew
    }

    if (transactionIsNew(tx.id())) queue.tryOffer(tx -> source)
  }

  private def validateFuture(tx: Transaction, allowRebroadcast: Boolean, source: Option[Channel]): Future[TracedResult[ValidationError, Boolean]] =
    Future(putIfNew(tx))(timedScheduler)
      .recover {
        case t =>
          log.warn(s"Error validating transaction ${tx.id()}", t)
          TracedResult(Left(GenericError(t)))
      }
      .andThen {
        case Success(TracedResult(Right(isNew), _)) if isNew || allowRebroadcast => broadcast(tx, source)
      }

  override def publish(tx: Transaction): TracedResult[ValidationError, Boolean] =
    Await.result(validateFuture(tx, settings.allowTxRebroadcasting, None), 10.seconds)

  override def close(): Unit = pollLoopCancelable.cancel()
}

object UtxPoolSynchronizer extends ScorexLogging {
  def apply(
      utx: UtxPool,
      settings: UtxSynchronizerSettings,
      allChannels: ChannelGroup,
      blockSource: Observable[LastBlockInfo],
      sc: Scheduler
  ): UtxPoolSynchronizer = new UtxPoolSynchronizerImpl(settings, tx => utx.putIfNew(tx), (tx, ch) => allChannels.broadcast(tx, ch), blockSource, sc)

}
