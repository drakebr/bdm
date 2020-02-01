package com.bdmplatform.it.sync.smartcontract.smartasset

import com.bdmplatform.account.AddressScheme
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.it.sync.{someAssetAmount, _}
import com.bdmplatform.it.transactions.BaseTransactionSuite
import com.bdmplatform.lang.v2.estimator.ScriptEstimatorV2
import com.bdmplatform.transaction.Asset.{IssuedAsset, Bdm}
import com.bdmplatform.transaction.Proofs
import com.bdmplatform.transaction.assets.BurnTransactionV2
import com.bdmplatform.transaction.smart.script.ScriptCompiler
import com.bdmplatform.transaction.transfer.TransferTransactionV2

import scala.concurrent.duration._

class NoOrderProofsSuite extends BaseTransactionSuite {
  val estimator = ScriptEstimatorV2
  test("try to use Order in asset scripts") {
    try {
      sender.issue(
        firstAddress,
        "assetWProofs",
        "Test coin for assetWProofs test",
        someAssetAmount,
        0,
        reissuable = true,
        issueFee,
        2,
        script = Some(
          ScriptCompiler(
            s"""
              |match tx {
              |  case o: Order => true
              |  case _ => false
              |}""".stripMargin,
            isAssetScript = true,
            estimator
          ).explicitGet()._1.bytes.value.base64)
      )

      fail("ScriptCompiler didn't throw expected error")
    } catch {
      case ex: java.lang.Exception => ex.getMessage should include("Compilation failed: Matching not exhaustive")
      case _: Throwable            => fail("ScriptCompiler works incorrect for orders with smart assets")
    }
  }

  test("try to use proofs in assets script") {
    val errProofMsg = "Reason: Proof doesn't validate as signature"
    val assetWProofs = sender
      .issue(
        firstAddress,
        "assetWProofs",
        "Test coin for assetWProofs test",
        someAssetAmount,
        0,
        reissuable = true,
        issueFee,
        2,
        script = Some(
          ScriptCompiler(
            s"""
                let proof = base58'assetWProofs'
                match tx {
                  case tx: SetAssetScriptTransaction | TransferTransaction | ReissueTransaction | BurnTransaction => tx.proofs[0] == proof
                  case _ => false
                }""".stripMargin,
            false,
            estimator
          ).explicitGet()._1.bytes.value.base64),
        waitForTx = true
      )
      .id

    val incorrectTrTx = TransferTransactionV2
      .create(
        IssuedAsset(ByteStr.decodeBase58(assetWProofs).get),
        pkByAddress(firstAddress),
        pkByAddress(thirdAddress),
        1,
        System.currentTimeMillis + 10.minutes.toMillis,
        Bdm,
        smartMinFee,
        Array.emptyByteArray,
        Proofs(Seq(ByteStr("assetWProofs".getBytes("UTF-8"))))
      )
      .right
      .get

    assertBadRequestAndMessage(
      sender.signedBroadcast(incorrectTrTx.json()),
      errProofMsg
    )

    val incorrectBrTx = BurnTransactionV2
      .create(
        AddressScheme.current.chainId,
        pkByAddress(firstAddress),
        IssuedAsset(ByteStr.decodeBase58(assetWProofs).get),
        1,
        smartMinFee,
        System.currentTimeMillis + 10.minutes.toMillis,
        Proofs(Seq(ByteStr("assetWProofs".getBytes("UTF-8"))))
      )
      .right
      .get

    assertBadRequestAndMessage(
      sender.signedBroadcast(incorrectBrTx.json()),
      errProofMsg
    )
  }

}
