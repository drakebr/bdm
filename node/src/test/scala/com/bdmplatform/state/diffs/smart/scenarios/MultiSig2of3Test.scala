package com.bdmplatform.state.diffs.smart.scenarios

import com.bdmplatform.account.PublicKey
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.lagonaki.mocks.TestBlock
import com.bdmplatform.lang.directives.values.{Expression, V1}
import com.bdmplatform.lang.script.v1.ExprScript
import com.bdmplatform.lang.utils._
import com.bdmplatform.lang.v1.compiler.ExpressionCompiler
import com.bdmplatform.lang.v1.compiler.Terms._
import com.bdmplatform.lang.v1.parser.Parser
import com.bdmplatform.state.diffs._
import com.bdmplatform.state.diffs.smart._
import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.transaction._
import com.bdmplatform.transaction.smart.SetScriptTransaction
import com.bdmplatform.transaction.transfer._
import com.bdmplatform.{NoShrink, TransactionGen, crypto}
import org.scalacheck.Gen
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.{ScalaCheckPropertyChecks => PropertyChecks}

class MultiSig2of3Test extends PropSpec with PropertyChecks with Matchers with TransactionGen with NoShrink {

  def multisigTypedExpr(pk0: PublicKey, pk1: PublicKey, pk2: PublicKey): EXPR = {
    val script =
      s"""
         |
         |let A = base58'${ByteStr(pk0)}'
         |let B = base58'${ByteStr(pk1)}'
         |let C = base58'${ByteStr(pk2)}'
         |
         |let proofs = tx.proofs
         |let AC = if(sigVerify(tx.bodyBytes,proofs[0],A)) then 1 else 0
         |let BC = if(sigVerify(tx.bodyBytes,proofs[1],B)) then 1 else 0
         |let CC = if(sigVerify(tx.bodyBytes,proofs[2],C)) then 1 else 0
         |
         | AC + BC+ CC >= 2
         |
      """.stripMargin
    val untyped = Parser.parseExpr(script).get.value
    ExpressionCompiler(compilerContext(V1, Expression, isAssetScript = false), untyped).explicitGet()._1
  }

  val preconditionsAndTransfer: Gen[(GenesisTransaction, SetScriptTransaction, TransferTransactionV2, Seq[ByteStr])] = for {
    master    <- accountGen
    s0        <- accountGen
    s1        <- accountGen
    s2        <- accountGen
    recepient <- accountGen
    ts        <- positiveIntGen
    genesis = GenesisTransaction.create(master, ENOUGH_AMT, ts).explicitGet()
    setSctipt <- selfSignedSetScriptTransactionGenP(master, ExprScript(multisigTypedExpr(s0, s1, s2)).explicitGet())
    amount    <- positiveLongGen
    fee       <- smallFeeGen
    timestamp <- timestampGen
  } yield {
    val unsigned =
      TransferTransactionV2
        .create(Bdm, master, recepient, amount, timestamp, Bdm, fee, Array.emptyByteArray, proofs = Proofs.empty)
        .explicitGet()
    val sig0 = ByteStr(crypto.sign(s0, unsigned.bodyBytes()))
    val sig1 = ByteStr(crypto.sign(s1, unsigned.bodyBytes()))
    val sig2 = ByteStr(crypto.sign(s2, unsigned.bodyBytes()))
    (genesis, setSctipt, unsigned, Seq(sig0, sig1, sig2))
  }

  property("2 of 3 multisig") {

    forAll(preconditionsAndTransfer) {
      case (genesis, script, transfer, sigs) =>
        val validProofs = Seq(
          transfer.copy(proofs = Proofs.create(Seq(sigs(0), sigs(1))).explicitGet()),
          transfer.copy(proofs = Proofs.create(Seq(ByteStr.empty, sigs(1), sigs(2))).explicitGet())
        )

        val invalidProofs = Seq(
          transfer.copy(proofs = Proofs.create(Seq(sigs(0))).explicitGet()),
          transfer.copy(proofs = Proofs.create(Seq(sigs(1))).explicitGet()),
          transfer.copy(proofs = Proofs.create(Seq(sigs(1), sigs(0))).explicitGet())
        )

        validProofs.foreach { tx =>
          assertDiffAndState(Seq(TestBlock.create(Seq(genesis, script))), TestBlock.create(Seq(tx)), smartEnabledFS) { case _ => () }
        }
        invalidProofs.foreach { tx =>
          assertLeft(Seq(TestBlock.create(Seq(genesis, script))), TestBlock.create(Seq(tx)), smartEnabledFS)("TransactionNotAllowedByScript")
        }
    }
  }

}
