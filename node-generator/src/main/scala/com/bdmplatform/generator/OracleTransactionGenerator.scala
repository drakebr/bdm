package com.bdmplatform.generator

import cats.Show
import com.bdmplatform.account.KeyPair
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.generator.OracleTransactionGenerator.Settings
import com.bdmplatform.generator.utils.Gen
import com.bdmplatform.it.util._
import com.bdmplatform.lang.v1.estimator.ScriptEstimator
import com.bdmplatform.state._
import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.transaction.smart.SetScriptTransaction
import com.bdmplatform.transaction.transfer.TransferTransactionV2
import com.bdmplatform.transaction.{DataTransaction, Transaction}

class OracleTransactionGenerator(settings: Settings, val accounts: Seq[KeyPair], estimator: ScriptEstimator) extends TransactionGenerator {
  override def next(): Iterator[Transaction] = generate(settings).toIterator

  def generate(settings: Settings): Seq[Transaction] = {
    val oracle = accounts.last

    val scriptedAccount = accounts.head

    val script = Gen.oracleScript(oracle, settings.requiredData, estimator)

    val enoughFee = 0.005.bdm

    val setScript: Transaction =
      SetScriptTransaction
        .selfSigned(scriptedAccount, Some(script), enoughFee, System.currentTimeMillis())
        .explicitGet()

    val setDataTx: Transaction = DataTransaction
      .selfSigned(oracle, settings.requiredData.toList, enoughFee, System.currentTimeMillis())
      .explicitGet()

    val now = System.currentTimeMillis()
    val transactions: List[Transaction] = (1 to settings.transactions).map { i =>
      TransferTransactionV2
        .selfSigned(Bdm, scriptedAccount, oracle, 1.bdm, now + i, Bdm, enoughFee, Array.emptyByteArray)
        .explicitGet()
    }.toList

    setScript +: setDataTx +: transactions
  }
}

object OracleTransactionGenerator {
  final case class Settings(transactions: Int, requiredData: Set[DataEntry[_]])

  object Settings {
    implicit val toPrintable: Show[Settings] = { x =>
      s"Transactions: ${x.transactions}\n" +
        s"DataEntries: ${x.requiredData}\n"
    }
  }
}
