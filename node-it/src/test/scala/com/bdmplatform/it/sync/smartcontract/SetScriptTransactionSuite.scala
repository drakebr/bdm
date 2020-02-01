package com.bdmplatform.it.sync.smartcontract

import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.crypto
import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.it.sync.{minFee, setScriptFee, transferAmount}
import com.bdmplatform.it.transactions.BaseTransactionSuite
import com.bdmplatform.it.util._
import com.bdmplatform.lang.v2.estimator.ScriptEstimatorV2
import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.transaction.Proofs
import com.bdmplatform.transaction.smart.SetScriptTransaction
import com.bdmplatform.transaction.smart.script.ScriptCompiler
import com.bdmplatform.transaction.transfer._
import org.scalatest.CancelAfterFailure

class SetScriptTransactionSuite extends BaseTransactionSuite with CancelAfterFailure {
  private val fourthAddress: String = sender.createAddress()

  private val acc0 = pkByAddress(firstAddress)
  private val acc1 = pkByAddress(secondAddress)
  private val acc2 = pkByAddress(thirdAddress)
  private val acc3 = pkByAddress(fourthAddress)

  test("setup acc0 with 1 bdm") {
    sender.transfer(
      sender.address,
      acc0.stringRepr,
      assetId = None,
      amount = 3 * transferAmount + 3 * (0.00001.bdm + 0.00002.bdm), // Script fee
      fee = minFee,
      version = 2,
      waitForTx = true
    )
  }

  test("set acc0 as 2of2 multisig") {
    val scriptText = s"""
        match tx {
          case t: Transaction => {
            let A = base58'${ByteStr(acc1.publicKey)}'
            let B = base58'${ByteStr(acc2.publicKey)}'
            let AC = sigVerify(tx.bodyBytes,tx.proofs[0],A)
            let BC = sigVerify(tx.bodyBytes,tx.proofs[1],B)
            AC && BC
          }
          case _ => false
        }
      """.stripMargin

    val script      = ScriptCompiler(scriptText, isAssetScript = false, ScriptEstimatorV2).explicitGet()._1.bytes().base64
    val setScriptId = sender.setScript(acc0.stringRepr, Some(script), setScriptFee, waitForTx = true).id

    val acc0ScriptInfo = sender.addressScriptInfo(acc0.stringRepr)

    acc0ScriptInfo.script.isEmpty shouldBe false
    acc0ScriptInfo.scriptText.isEmpty shouldBe false

    acc0ScriptInfo.script.get.startsWith("base64:") shouldBe true

    sender.transactionInfo(setScriptId).script.get.startsWith("base64:") shouldBe true
  }

  test("can't send from acc0 using old pk") {
    assertApiErrorRaised(
      sender.transfer(
        acc0.stringRepr,
        recipient = acc3.stringRepr,
        assetId = None,
        amount = transferAmount,
        fee = minFee + 0.00001.bdm + 0.00002.bdm,
      )
    )
  }

  test("can send from acc0 using multisig of acc1 and acc2") {
    val unsigned =
      TransferTransactionV2
        .create(
          assetId = Bdm,
          sender = acc0,
          recipient = acc3,
          amount = transferAmount,
          timestamp = System.currentTimeMillis(),
          feeAssetId = Bdm,
          feeAmount = minFee + 0.004.bdm,
          attachment = Array.emptyByteArray,
          proofs = Proofs.empty
        )
        .explicitGet()
    val sig1 = ByteStr(crypto.sign(acc1, unsigned.bodyBytes()))
    val sig2 = ByteStr(crypto.sign(acc2, unsigned.bodyBytes()))

    val signed = unsigned.copy(proofs = Proofs(Seq(sig1, sig2)))

    sender.signedBroadcast(signed.json(), waitForTx = true).id

  }

  test("can clear script at acc0") {
    val unsigned = SetScriptTransaction
      .create(
        sender = acc0,
        script = None,
        fee = setScriptFee + 0.004.bdm,
        timestamp = System.currentTimeMillis(),
        proofs = Proofs.empty
      )
      .explicitGet()
    val sig1 = ByteStr(crypto.sign(acc1, unsigned.bodyBytes()))
    val sig2 = ByteStr(crypto.sign(acc2, unsigned.bodyBytes()))

    val signed = unsigned.copy(proofs = Proofs(Seq(sig1, sig2)))

    val removeScriptId =  sender
      .signedBroadcast(signed.json(), waitForTx = true).id

    sender.transactionInfo(removeScriptId).script shouldBe None

  }

  test("can send using old pk of acc0") {
    sender.transfer(
      acc0.stringRepr,
      recipient = acc3.stringRepr,
      assetId = None,
      amount = transferAmount,
      fee = minFee,
      version = 2,
      waitForTx = true
    )
  }
}
