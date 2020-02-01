package com.bdmplatform.state.diffs.smart.scenarios

import com.bdmplatform.common.utils._
import com.bdmplatform.lagonaki.mocks.TestBlock
import com.bdmplatform.lang.directives.values.{Expression, V3}
import com.bdmplatform.lang.script.v1.ExprScript
import com.bdmplatform.lang.utils.compilerContext
import com.bdmplatform.lang.v1.compiler.ExpressionCompiler
import com.bdmplatform.lang.v1.parser.Parser
import com.bdmplatform.state.BinaryDataEntry
import com.bdmplatform.state.diffs.smart.smartEnabledFS
import com.bdmplatform.state.diffs.{ENOUGH_AMT, assertDiffEi}
import com.bdmplatform.transaction.smart.SetScriptTransaction
import com.bdmplatform.transaction.transfer.TransferTransaction
import com.bdmplatform.transaction.{DataTransaction, GenesisTransaction}
import com.bdmplatform.{NoShrink, TransactionGen}
import org.scalacheck.Gen
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class TransferByIdTest extends PropSpec with ScalaCheckPropertyChecks with Matchers with TransactionGen with NoShrink {

  val scriptSrc =
    s"""
       |match tx {
       |  case dtx: DataTransaction =>
       |    let txId    = extract(getBinary(dtx.data, "transfer_id"))
       |    let maybeTx = transferTransactionById(txId)
       |
       |    isDefined(maybeTx)
       |
       |  case other => false
       |}
     """.stripMargin

  val expr = {
    val parsed = Parser.parseExpr(scriptSrc).get.value
    ExpressionCompiler(compilerContext(V3, Expression, isAssetScript = false), parsed).explicitGet()._1
  }

  def preconditions: Gen[(GenesisTransaction, TransferTransaction, SetScriptTransaction, DataTransaction)] =
    for {
      master    <- accountGen
      recipient <- accountGen
      ts        <- positiveIntGen
      genesis = GenesisTransaction.create(master, ENOUGH_AMT, ts).explicitGet()
      setScript <- selfSignedSetScriptTransactionGenP(master, ExprScript(V3, expr).explicitGet())
      transfer <- Gen.oneOf[TransferTransaction](
        transferGeneratorP(ts, master, recipient.toAddress, ENOUGH_AMT / 2),
        transferGeneratorPV2(ts, master, recipient.toAddress, ENOUGH_AMT / 2)
      )
      data <- dataTransactionGenP(master, List(BinaryDataEntry("transfer_id", transfer.id())))
    } yield (genesis, transfer, setScript, data)

  property("Transfer by id works fine") {
    forAll(preconditions) {
      case (genesis, transfer, setScript, data) =>
        assertDiffEi(
          Seq(TestBlock.create(Seq(genesis, transfer))),
          TestBlock.create(Seq(setScript, data)),
          smartEnabledFS
        )(_ shouldBe an[Right[_, _]])
    }
  }
}
