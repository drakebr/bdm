package com.bdmplatform.it.sync.smartcontract

import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.transaction.smart.SetScriptTransaction
import com.bdmplatform.transaction.smart.script.ScriptCompiler
import com.bdmplatform.transaction.transfer.TransferTransactionV2
import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.it.sync._
import com.bdmplatform.it.transactions.BaseTransactionSuite
import com.bdmplatform.it.util._
import org.scalatest.CancelAfterFailure
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.lang.v2.estimator.ScriptEstimatorV2

class BigLetChain extends BaseTransactionSuite with CancelAfterFailure {
  test("big let assignment chain") {
    val count = 550
    val scriptText =
      s"""
         | {-# STDLIB_VERSION 3    #-}
         | {-# CONTENT_TYPE   DAPP #-}
         |
         | @Verifier(tx)
         | func verify() = {
         |   let a0 = 1
         |   ${1 to count map (i => s"let a$i = a${i - 1}") mkString "\n"}
         |   a$count == a$count
         | }
       """.stripMargin

    val compiledScript = ScriptCompiler.compile(scriptText, ScriptEstimatorV2).explicitGet()._1

    val newAddress   = sender.createAddress()
    val acc0         = pkByAddress(firstAddress)
    val pkNewAddress = pkByAddress(newAddress)

    sender.transfer(acc0.stringRepr, newAddress, 10.bdm, minFee, waitForTx = true)

    val scriptSet = SetScriptTransaction.selfSigned(
      pkNewAddress,
      Some(compiledScript),
      setScriptFee,
      System.currentTimeMillis()
    )
    val scriptSetBroadcast = sender.signedBroadcast(scriptSet.explicitGet().json.value)
    nodes.waitForHeightAriseAndTxPresent(scriptSetBroadcast.id)

    val transfer = TransferTransactionV2.selfSigned(
      Bdm,
      pkNewAddress,
      pkNewAddress,
      1.bdm,
      System.currentTimeMillis(),
      Bdm,
      smartMinFee,
      Array()
    )
    val transferBroadcast = sender.signedBroadcast(transfer.explicitGet().json.value)
    nodes.waitForHeightAriseAndTxPresent(transferBroadcast.id)
  }
}
