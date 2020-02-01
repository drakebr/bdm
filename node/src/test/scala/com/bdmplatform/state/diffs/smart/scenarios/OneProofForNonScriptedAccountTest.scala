package com.bdmplatform.state.diffs.smart.scenarios

import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.lagonaki.mocks.TestBlock
import com.bdmplatform.lang.v1.compiler.Terms._
import com.bdmplatform.state.diffs.smart.smartEnabledFS
import com.bdmplatform.state.diffs.{ENOUGH_AMT, assertDiffEi, produce}
import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.lang.script.v1.ExprScript
import com.bdmplatform.transaction.transfer._
import com.bdmplatform.transaction.{GenesisTransaction, Proofs}
import com.bdmplatform.{NoShrink, TransactionGen}
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.{ScalaCheckPropertyChecks => PropertyChecks}

class OneProofForNonScriptedAccountTest extends PropSpec with PropertyChecks with Matchers with TransactionGen with NoShrink {

  property("exactly 1 proof required for non-scripted accounts") {
    val s = for {
      master    <- accountGen
      recepient <- accountGen
      amt       <- positiveLongGen
      fee       <- smallFeeGen
      ts        <- positiveIntGen
      genesis = GenesisTransaction.create(master, ENOUGH_AMT, ts).explicitGet()
      setScript <- selfSignedSetScriptTransactionGenP(master, ExprScript(TRUE).explicitGet())
      transfer = TransferTransactionV2.selfSigned(Bdm, master, recepient, amt, ts, Bdm, fee, Array.emptyByteArray).explicitGet()
    } yield (genesis, setScript, transfer)

    forAll(s) {
      case (genesis, script, transfer) =>
        val transferWithExtraProof = transfer.copy(proofs = Proofs(Seq(ByteStr.empty, ByteStr(Array(1: Byte)))))
        assertDiffEi(Seq(TestBlock.create(Seq(genesis))), TestBlock.create(Seq(transferWithExtraProof)), smartEnabledFS)(totalDiffEi =>
          totalDiffEi should produce("must have exactly 1 proof"))
    }
  }

}
