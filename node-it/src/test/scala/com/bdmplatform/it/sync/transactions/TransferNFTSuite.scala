package com.bdmplatform.it.sync.transactions

import com.bdmplatform.account.KeyPair
import com.bdmplatform.it.transactions.BaseTransactionSuite
import com.bdmplatform.it.NTPTime
import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.it.sync.{calcMassTransferFee, setScriptFee}
import com.bdmplatform.it.util._
import com.bdmplatform.it.sync._
import com.bdmplatform.lang.v2.estimator.ScriptEstimatorV2
import com.bdmplatform.transaction.smart.script.ScriptCompiler
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.it.api.Transaction
import com.bdmplatform.lang.v1.compiler.Terms
import com.bdmplatform.transaction.Asset
import com.bdmplatform.transaction.assets.exchange.{AssetPair, ExchangeTransactionV2, Order}
import com.bdmplatform.transaction.smart.InvokeScriptTransaction
import com.bdmplatform.transaction.transfer.MassTransferTransaction.Transfer

class TransferNFTSuite extends BaseTransactionSuite with NTPTime {
  val assetName        = "NFTAsset"
  val assetDescription = "my asset description"

  test("NFT should be correctly transferred via transfer transaction"){
    val nftAsset = sender.issue(firstAddress, assetName, assetDescription, 1, 0, reissuable = false, 1.bdm / 1000, waitForTx = true).id
    sender.transfer(firstAddress, secondAddress, 1, minFee, Some(nftAsset), waitForTx = true)

    sender.assetBalance(firstAddress, nftAsset).balance shouldBe 0
    sender.nftAssetsBalance(firstAddress, 10).map(info => info.assetId) shouldNot contain (nftAsset)
    sender.assetBalance(secondAddress, nftAsset).balance shouldBe 1
    sender.nftAssetsBalance(secondAddress, 10).map(info => info.assetId) should contain (nftAsset)
  }

  test("NFT should be correctly transferred via invoke script transaction") {
    val nftAsset = sender.issue(firstAddress, assetName, assetDescription, 1, 0, reissuable = false, 1.bdm / 1000, waitForTx = true).id
    val dApp = secondAddress
    val scriptText =
      s"""
         |{-# STDLIB_VERSION 3 #-}
         |{-# CONTENT_TYPE DAPP #-}
         |{-# SCRIPT_TYPE ACCOUNT #-}
         |
         |@Callable(i)
         |func nftTransferToDapp() = {
         |    let pmt = i.payment.extract()
         |    TransferSet([
         |            ScriptTransfer(this, pmt.amount, pmt.assetId)
         |        ])
         |}
         |
         |@Callable(i)
         |func nftPaymentTransferToThirdAddress(address: String) = {
         |    let thirdAddress = Address(fromBase58String(address))
         |    let pmt = i.payment.extract()
         |    TransferSet([
         |            ScriptTransfer(thirdAddress, pmt.amount, pmt.assetId)
         |        ])
         |}
         |
         |@Callable(i)
         |func transferAsPayment() = {
         |    TransferSet([])
         |    }
         |
         |@Callable(i)
         |func nftTransferToSelf() = {
         |    let pmt = i.payment.extract()
         |    TransferSet([
         |            ScriptTransfer(i.caller, pmt.amount, pmt.assetId)
         |        ])
         |}
         |@Callable(i)
         |func transferFromDappToAddress(address: String) = {
         |    let recipient = Address(fromBase58String(address))
         |    TransferSet([
         |            ScriptTransfer(recipient, 1, base58'$nftAsset')
         |        ])
         |}
         |@Verifier(t)
         |func verify() = {
         | true
         |}
        """.stripMargin
    val script      = ScriptCompiler.compile(scriptText, ScriptEstimatorV2).explicitGet()._1.bytes().base64
    sender.setScript(dApp, Some(script), setScriptFee, waitForTx = true)
    def invokeTransfer(caller: String, functionName: String, args: List[Terms.EXPR] = List.empty, payment: Seq[InvokeScriptTransaction.Payment] = Seq.empty): Transaction = {
    sender.invokeScript(
      caller,
      dApp,
      Some(functionName),
      payment = payment,
      args = args,
      fee = 1300000,
      waitForTx = true)._1
    }
    val nftPayment = Seq(InvokeScriptTransaction.Payment(1, Asset.fromString(Some(nftAsset))))

    invokeTransfer(firstAddress,"nftTransferToDapp",payment = nftPayment)
    sender.assetBalance(firstAddress, nftAsset).balance shouldBe 0
    sender.nftAssetsBalance(firstAddress, 10).map(info => info.assetId) shouldNot contain (nftAsset)
    sender.assetBalance(dApp, nftAsset).balance shouldBe 1
    sender.nftAssetsBalance(dApp, 10).map(info => info.assetId) should contain (nftAsset)

    invokeTransfer(firstAddress, "transferFromDappToAddress",args = List(Terms.CONST_STRING(thirdAddress).explicitGet()))
    sender.assetBalance(dApp, nftAsset).balance shouldBe 0
    sender.nftAssetsBalance(dApp, 10).map(info => info.assetId) shouldNot contain (nftAsset)
    sender.assetBalance(thirdAddress, nftAsset).balance shouldBe 1
    sender.nftAssetsBalance(thirdAddress, 10).map(info => info.assetId) should contain (nftAsset)

    invokeTransfer(thirdAddress, "nftTransferToSelf",payment = Seq(InvokeScriptTransaction.Payment(1, Asset.fromString(Some(nftAsset)))))
    sender.assetBalance(dApp, nftAsset).balance shouldBe 0
    sender.nftAssetsBalance(dApp, 10).map(info => info.assetId) shouldNot contain (nftAsset)
    sender.assetBalance(thirdAddress, nftAsset).balance shouldBe 1
    sender.nftAssetsBalance(thirdAddress, 10).map(info => info.assetId) should contain (nftAsset)

    invokeTransfer(thirdAddress, "nftPaymentTransferToThirdAddress",
      args = List(Terms.CONST_STRING(firstAddress).explicitGet()),
      payment = Seq(InvokeScriptTransaction.Payment(1, Asset.fromString(Some(nftAsset)))))
    sender.assetBalance(thirdAddress, nftAsset).balance shouldBe 0
    sender.nftAssetsBalance(thirdAddress, 10).map(info => info.assetId) shouldNot contain (nftAsset)
    sender.assetBalance(dApp, nftAsset).balance shouldBe 0
    sender.nftAssetsBalance(dApp, 10).map(info => info.assetId) shouldNot contain (nftAsset)
    sender.assetBalance(firstAddress, nftAsset).balance shouldBe 1
    sender.nftAssetsBalance(firstAddress, 10).map(info => info.assetId) should contain (nftAsset)

    invokeTransfer(firstAddress, "transferAsPayment",payment = Seq(InvokeScriptTransaction.Payment(1, Asset.fromString(Some(nftAsset)))))
    sender.assetBalance(firstAddress, nftAsset).balance shouldBe 0
    sender.nftAssetsBalance(firstAddress, 10).map(info => info.assetId) shouldNot contain (nftAsset)
    sender.assetBalance(dApp, nftAsset).balance shouldBe 1
    sender.nftAssetsBalance(dApp, 10).map(info => info.assetId) should contain (nftAsset)
  }

  test("NFT should be correctly transferred via mass transfer transaction") {
    val nftAsset = sender.issue(firstAddress, assetName, assetDescription, 1, 0, reissuable = false, 1.bdm / 1000, waitForTx = true).id
    sender.massTransfer(firstAddress, List(Transfer(thirdAddress, 1)), calcMassTransferFee(1), Some(nftAsset), waitForTx = true)

    sender.assetBalance(firstAddress, nftAsset).balance shouldBe 0
    sender.nftAssetsBalance(firstAddress, 10).map(info => info.assetId) shouldNot contain (nftAsset)
    sender.assetBalance(thirdAddress, nftAsset).balance shouldBe 1
    sender.nftAssetsBalance(thirdAddress, 10).map(info => info.assetId) should contain (nftAsset)
  }

  test("NFT should correctly be transferred via exchange transaction") {
    val buyer = KeyPair("buyer".getBytes("UTF-8"))
    val seller  = KeyPair("seller".getBytes("UTF-8"))
    val matcher = KeyPair("matcher".getBytes("UTF-8"))
    val transfers = List(Transfer(buyer.stringRepr, 10.bdm), Transfer(seller.stringRepr, 10.bdm), Transfer(matcher.stringRepr, 10.bdm))
    sender.massTransfer(firstAddress, transfers, calcMassTransferFee(transfers.size), waitForTx = true)

    val nftAsset = sender.broadcastIssue(seller, assetName, assetDescription, 1, 0, reissuable = false, 1.bdm / 1000, waitForTx = true, script = None).id
    val pair = AssetPair.createAssetPair(nftAsset,"BDM")
    val ts = ntpTime.correctedTime()
    val buy = Order.buy(buyer, matcher, pair.get, 1, 1.bdm, ts, ts + Order.MaxLiveTime, matcherFee)
    val sell = Order.sell(seller, matcher, pair.get, 1, 1.bdm, ts, ts + Order.MaxLiveTime, matcherFee)

    val tx = ExchangeTransactionV2
      .create(
        matcher = matcher,
        buyOrder = buy,
        sellOrder = sell,
        amount = 1,
        price = 1.bdm,
        buyMatcherFee = matcherFee,
        sellMatcherFee = matcherFee,
        fee = matcherFee,
        timestamp = ts
      ).explicitGet().json()

    sender.signedBroadcast(tx, waitForTx = true)
    sender.nftAssetsBalance(buyer.stringRepr, 10).map(info => info.assetId) should contain oneElementOf List(nftAsset)
    sender.nftAssetsBalance(seller.stringRepr, 10).map(info => info.assetId) shouldNot contain atLeastOneElementOf List(nftAsset)
    sender.assetBalance(buyer.stringRepr, nftAsset).balance shouldBe 1
    sender.assetBalance(seller.stringRepr, nftAsset).balance shouldBe 0

  }

}
