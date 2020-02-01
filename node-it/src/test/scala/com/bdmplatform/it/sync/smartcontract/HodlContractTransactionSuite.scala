package com.bdmplatform.it.sync.smartcontract

import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.it.sync.{minFee, setScriptFee}
import com.bdmplatform.it.transactions.BaseTransactionSuite
import com.bdmplatform.it.util._
import com.bdmplatform.lang.v1.compiler.Terms.CONST_LONG
import com.bdmplatform.lang.v2.estimator.ScriptEstimatorV2
import com.bdmplatform.state._
import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.transaction.smart.InvokeScriptTransaction
import com.bdmplatform.transaction.smart.script.ScriptCompiler
import org.scalatest.CancelAfterFailure

class HodlContractTransactionSuite extends BaseTransactionSuite with CancelAfterFailure {

  private val contract = pkByAddress(firstAddress)
  private val caller   = pkByAddress(secondAddress)

  test("setup contract account with bdm") {
    sender
      .transfer(
        sender.address,
        recipient = contract.stringRepr,
        assetId = None,
        amount = 5.bdm,
        fee = minFee,
        waitForTx = true
      )
      .id
  }

  test("setup caller account with bdm") {
    sender
      .transfer(
        sender.address,
        recipient = caller.stringRepr,
        assetId = None,
        amount = 10.bdm,
        fee = minFee,
        waitForTx = true
      )
      .id
  }

  test("set contract to contract account") {
    val scriptText =
      """
        |{-# STDLIB_VERSION 3 #-}
        |{-# CONTENT_TYPE DAPP #-}
        |
        |	@Callable(i)
        |	func deposit() = {
        |   let pmt = extract(i.payment)
        |   if (isDefined(pmt.assetId)) then throw("can hodl bdm only at the moment")
        |   else {
        |	  	let currentKey = toBase58String(i.caller.bytes)
        |	  	let currentAmount = match getInteger(this, currentKey) {
        |	  		case a:Int => a
        |	  		case _ => 0
        |	  	}
        |	  	let newAmount = currentAmount + pmt.amount
        |	  	WriteSet([DataEntry(currentKey, newAmount)])
        |
        |   }
        |	}
        |
        | @Callable(i)
        | func withdraw(amount: Int) = {
        |	  	let currentKey = toBase58String(i.caller.bytes)
        |	  	let currentAmount = match getInteger(this, currentKey) {
        |	  		case a:Int => a
        |	  		case _ => 0
        |	  	}
        |		let newAmount = currentAmount - amount
        |	 if (amount < 0)
        |			then throw("Can't withdraw negative amount")
        |  else if (newAmount < 0)
        |			then throw("Not enough balance")
        |			else  ScriptResult(
        |					WriteSet([DataEntry(currentKey, newAmount)]),
        |					TransferSet([ScriptTransfer(i.caller, amount, unit)])
        |				)
        |	}
        """.stripMargin

    val script      = ScriptCompiler.compile(scriptText, ScriptEstimatorV2).explicitGet()._1.bytes().base64
    val setScriptId = sender.setScript(contract.stringRepr, Some(script), setScriptFee, waitForTx = true).id

    val acc0ScriptInfo = sender.addressScriptInfo(contract.stringRepr)

    acc0ScriptInfo.script.isEmpty shouldBe false
    acc0ScriptInfo.scriptText.isEmpty shouldBe false
    acc0ScriptInfo.script.get.startsWith("base64:") shouldBe true

    sender.transactionInfo(setScriptId).script.get.startsWith("base64:") shouldBe true
  }

  test("caller deposits bdm") {
    val balanceBefore = sender.accountBalances(contract.stringRepr)._1
    val invokeScriptId = sender
      .invokeScript(
        caller.stringRepr,
        dappAddress = contract.stringRepr,
        func = Some("deposit"),
        args = List.empty,
        payment = Seq(InvokeScriptTransaction.Payment(1.5.bdm, Bdm)),
        fee = 1.bdm,
        waitForTx = true
      )
      ._1.id

    sender.waitForTransaction(invokeScriptId)

    sender.getDataByKey(contract.stringRepr, caller.stringRepr) shouldBe IntegerDataEntry(caller.stringRepr, 1.5.bdm)
    val balanceAfter = sender.accountBalances(contract.stringRepr)._1

    (balanceAfter - balanceBefore) shouldBe 1.5.bdm
  }

  test("caller can't withdraw more than owns") {
    assertBadRequestAndMessage(
      sender.invokeScript(
        caller.stringRepr,
        contract.stringRepr,
        func = Some("withdraw"),
        args = List(CONST_LONG(1.51.bdm)),
        payment = Seq(),
        fee = 1.bdm
      ),
      "Not enough balance"
    )
  }

  test("caller can withdraw less than he owns") {
    val balanceBefore = sender.accountBalances(contract.stringRepr)._1
    val invokeScriptId = sender
      .invokeScript(
        caller.stringRepr,
        dappAddress = contract.stringRepr,
        func = Some("withdraw"),
        args = List(CONST_LONG(1.49.bdm)),
        payment = Seq(),
        fee = 1.bdm,
        waitForTx = true
      )
      ._1.id

    val balanceAfter = sender.accountBalances(contract.stringRepr)._1

    sender.getDataByKey(contract.stringRepr, caller.stringRepr) shouldBe IntegerDataEntry(caller.stringRepr, 0.01.bdm)
    (balanceAfter - balanceBefore) shouldBe -1.49.bdm

    val stateChangesInfo = sender.debugStateChanges(invokeScriptId).stateChanges

    val stateChangesData = stateChangesInfo.get.data.head
    stateChangesInfo.get.data.length shouldBe 1
    stateChangesData.`type` shouldBe "integer"
    stateChangesData.value shouldBe 0.01.bdm

    val stateChangesTransfers = stateChangesInfo.get.transfers.head
    stateChangesInfo.get.transfers.length shouldBe 1
    stateChangesTransfers.address shouldBe caller.stringRepr
    stateChangesTransfers.amount shouldBe 1.49.bdm
    stateChangesTransfers.asset shouldBe None
  }

}
