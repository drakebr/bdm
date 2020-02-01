package com.bdmplatform.it.sync.smartcontract

import com.bdmplatform.account.{AddressScheme, Alias}
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.it.sync.{minFee, setScriptFee}
import com.bdmplatform.it.transactions.BaseTransactionSuite
import com.bdmplatform.lang.v1.FunctionHeader
import com.bdmplatform.lang.v1.compiler.Terms
import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.transaction.CreateAliasTransactionV2
import com.bdmplatform.transaction.smart.SetScriptTransaction
import com.bdmplatform.transaction.smart.script.ScriptCompiler
import com.bdmplatform.lang.script.v1.ExprScript
import com.bdmplatform.lang.v2.estimator.ScriptEstimatorV2
import com.bdmplatform.transaction.transfer.TransferTransactionV2
import org.scalatest.CancelAfterFailure

class ScriptExecutionErrorSuite extends BaseTransactionSuite with CancelAfterFailure {
  private val acc0 = pkByAddress(firstAddress)
  private val acc1 = pkByAddress(secondAddress)
  private val acc2 = pkByAddress(thirdAddress)
  private val ts   = System.currentTimeMillis()

  test("custom throw message") {
    val scriptSrc =
      """
        |match tx {
        |  case t : TransferTransaction =>
        |    let res = if isDefined(t.assetId) then extract(t.assetId) == base58'' else isDefined(t.assetId) == false
        |    res
        |  case s : SetScriptTransaction => true
        |  case other => throw("Your transaction has incorrect type.")
        |}
      """.stripMargin

    val compiled = ScriptCompiler(scriptSrc, isAssetScript = false, ScriptEstimatorV2).explicitGet()._1

    val tx = sender.signedBroadcast(SetScriptTransaction.selfSigned(acc2, Some(compiled), setScriptFee, ts).explicitGet().json())
    nodes.waitForHeightAriseAndTxPresent(tx.id)

    val alias = Alias.fromString(s"alias:${AddressScheme.current.chainId.toChar}:asdasdasdv").explicitGet()
    assertBadRequestAndResponse(
      sender.signedBroadcast(CreateAliasTransactionV2.selfSigned(acc2, alias, minFee, ts).explicitGet().json()),
      "Your transaction has incorrect type."
    )
  }

  test("wrong type of script return value") {
    val script = ExprScript(
      Terms.FUNCTION_CALL(
        FunctionHeader.Native(100),
        List(Terms.CONST_LONG(3), Terms.CONST_LONG(2))
      )
    ).explicitGet()

    val tx = sender.signAndBroadcast(
      SetScriptTransaction
        .selfSigned(acc0, Some(script), setScriptFee, ts)
        .explicitGet()
        .json())
    nodes.waitForHeightAriseAndTxPresent(tx.id)

    assertBadRequestAndResponse(
      sender.signedBroadcast(
        TransferTransactionV2
          .selfSigned(Bdm, acc0, acc1.toAddress, 1000, ts, Bdm, minFee, Array())
          .explicitGet()
          .json()),
      "not a boolean"
    )
  }
}
