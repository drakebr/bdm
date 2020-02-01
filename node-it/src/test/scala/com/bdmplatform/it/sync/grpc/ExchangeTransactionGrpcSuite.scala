package com.bdmplatform.it.sync.grpc

import com.bdmplatform.it.NTPTime
import com.bdmplatform.it.sync.{matcherFee, minFee, someAssetAmount}
import com.bdmplatform.transaction.assets.IssueTransactionV1
import com.bdmplatform.it.util._
import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.protobuf.transaction.{PBTransactions, Recipient}
import com.bdmplatform.transaction.Asset.{IssuedAsset, Bdm}
import com.bdmplatform.transaction.assets.exchange.{AssetPair, Order}
import com.bdmplatform.common.utils.EitherExt2
import io.grpc.Status.Code

class ExchangeTransactionGrpcSuite extends GrpcBaseTransactionSuite with NTPTime {

  val transactionV1versions = (1: Byte, 1: Byte, 1: Byte)
  val transactionV2versions = for {
    o1ver <- 1 to 3
    o2ver <- 1 to 3
  } yield (o1ver.toByte, o2ver.toByte, 2.toByte)

  val (buyer, buyerAddress) = (firstAcc, firstAddress)
  val (seller, sellerAddress) = (secondAcc, secondAddress)
  val (matcher, matcherAddress) = (thirdAcc, thirdAddress)


  val versions = transactionV1versions +: transactionV2versions

  test("exchange tx with orders v1,v2") {
    val exchAsset = sender.grpc.broadcastIssue(buyer, "exchAsset", someAssetAmount, 8, true, 1.bdm, waitForTx = true)
    val exchAssetId = PBTransactions.vanilla(exchAsset).explicitGet().id().base58
    val price               = 500000L
    val amount              = 40000000L
    val priceAssetSpending = amount * price / 100000000L
    val pair = AssetPair.createAssetPair("BDM", exchAssetId).get
    for ((o1ver, o2ver, tver) <- versions) {
      val ts                  = ntpTime.correctedTime()
      val expirationTimestamp = ts + Order.MaxLiveTime
      val buy  = Order.buy(buyer, matcher, pair, amount, price, ts, expirationTimestamp, matcherFee, o1ver)
      val sell = Order.sell(seller, matcher, pair, amount, price, ts, expirationTimestamp, matcherFee, o2ver)
      val buyerBdmBalanceBefore = sender.grpc.bdmBalance(buyerAddress).available
      val sellerBdmBalanceBefore = sender.grpc.bdmBalance(sellerAddress).available
      val buyerAssetBalanceBefore = sender.grpc.assetsBalance(buyerAddress, Seq(exchAssetId)).getOrElse(exchAssetId, 0L)
      val sellerAssetBalanceBefore = sender.grpc.assetsBalance(sellerAddress, Seq(exchAssetId)).getOrElse(exchAssetId, 0L)

      sender.grpc.exchange(matcher, buy, sell, amount, price, matcherFee, matcherFee, matcherFee, ts, tver, waitForTx = true)

      sender.grpc.bdmBalance(buyerAddress).available shouldBe buyerBdmBalanceBefore + amount - matcherFee
      sender.grpc.bdmBalance(sellerAddress).available shouldBe sellerBdmBalanceBefore - amount - matcherFee
      sender.grpc.assetsBalance(buyerAddress, Seq(exchAssetId))(exchAssetId) shouldBe buyerAssetBalanceBefore - priceAssetSpending
      sender.grpc.assetsBalance(sellerAddress, Seq(exchAssetId))(exchAssetId) shouldBe sellerAssetBalanceBefore + priceAssetSpending
    }
  }

  test("exchange tx with orders v3") {
    val feeAsset = sender.grpc.broadcastIssue(buyer, "feeAsset", someAssetAmount, 8, true, 1.bdm, waitForTx = true)
    val feeAssetId = PBTransactions.vanilla(feeAsset).explicitGet().id()
    val price = 500000L
    val amount = 40000000L
    val priceAssetSpending = price * amount / 100000000L

    for ((o1ver, o2ver, matcherFeeOrder1, matcherFeeOrder2, buyerBdmDelta, sellerBdmDelta, buyerAssetDelta, sellerAssetDelta) <- Seq(
      (1: Byte, 3: Byte, Bdm, IssuedAsset(feeAssetId), amount - matcherFee, -amount, -priceAssetSpending, priceAssetSpending - matcherFee),
      (1: Byte, 3: Byte, Bdm, Bdm, amount - matcherFee, -amount - matcherFee, -priceAssetSpending, priceAssetSpending),
      (2: Byte, 3: Byte, Bdm, IssuedAsset(feeAssetId), amount - matcherFee, -amount, -priceAssetSpending, priceAssetSpending - matcherFee),
      (3: Byte, 1: Byte, IssuedAsset(feeAssetId), Bdm, amount, -amount - matcherFee, -priceAssetSpending - matcherFee, priceAssetSpending),
      (2: Byte, 3: Byte, Bdm, Bdm, amount - matcherFee, -amount - matcherFee, -priceAssetSpending, priceAssetSpending),
      (3: Byte, 2: Byte, IssuedAsset(feeAssetId), Bdm,  amount, -amount - matcherFee, -priceAssetSpending - matcherFee, priceAssetSpending),
    )) {
      if (matcherFeeOrder1 == Bdm && matcherFeeOrder2 != Bdm) {
        sender.grpc.broadcastTransfer(buyer, Recipient().withAddress(sellerAddress), 100000, minFee, assetId = feeAssetId.base58, waitForTx = true)
      }

      val buyerBdmBalanceBefore = sender.grpc.bdmBalance(buyerAddress).available
      val sellerBdmBalanceBefore = sender.grpc.bdmBalance(sellerAddress).available
      val buyerAssetBalanceBefore = sender.grpc.assetsBalance(buyerAddress, Seq(feeAssetId.base58)).getOrElse(feeAssetId.base58, 0L)
      val sellerAssetBalanceBefore = sender.grpc.assetsBalance(sellerAddress, Seq(feeAssetId.base58)).getOrElse(feeAssetId.base58, 0L)

      val ts                       = ntpTime.correctedTime()
      val expirationTimestamp      = ts + Order.MaxLiveTime
      val assetPair  = AssetPair.createAssetPair("BDM", feeAssetId.base58).get
      val buy        = Order.buy(buyer, matcher, assetPair, amount, price, ts, expirationTimestamp, matcherFee, o1ver, matcherFeeOrder1)
      val sell       = Order.sell(seller, matcher, assetPair, amount, price, ts, expirationTimestamp, matcherFee, o2ver, matcherFeeOrder2)

      sender.grpc.exchange(matcher, buy, sell, amount, price, matcherFee, matcherFee, matcherFee, ts, 2, waitForTx = true)

      sender.grpc.bdmBalance(buyerAddress).available shouldBe (buyerBdmBalanceBefore + buyerBdmDelta)
      sender.grpc.bdmBalance(sellerAddress).available shouldBe (sellerBdmBalanceBefore + sellerBdmDelta)
      sender.grpc.assetsBalance(buyerAddress, Seq(feeAssetId.base58))(feeAssetId.base58) shouldBe (buyerAssetBalanceBefore + buyerAssetDelta)
      sender.grpc.assetsBalance(sellerAddress, Seq(feeAssetId.base58))(feeAssetId.base58) shouldBe (sellerAssetBalanceBefore + sellerAssetDelta)
    }
  }

  test("cannot exchange non-issued assets") {
    val exchAsset: IssueTransactionV1 = IssueTransactionV1
      .selfSigned(
        sender = sender.privateKey,
        name = "myasset".getBytes("UTF-8"),
        description = "my asset description".getBytes("UTF-8"),
        quantity = someAssetAmount,
        decimals = 2,
        reissuable = true,
        fee = 1.bdm,
        timestamp = System.currentTimeMillis()
      )
      .right
      .get
    for ((o1ver, o2ver, tver) <- versions) {

      val assetId = exchAsset.id().base58
      val ts                  = ntpTime.correctedTime()
      val expirationTimestamp = ts + Order.MaxLiveTime
      val price               = 2 * Order.PriceConstant
      val amount              = 1
      val pair = AssetPair.createAssetPair("BDM", assetId).get
      val buy  = Order.buy(buyer, matcher, pair, amount, price, ts, expirationTimestamp, matcherFee, o1ver)
      val sell = Order.sell(seller, matcher, pair, amount, price, ts, expirationTimestamp, matcherFee, o2ver)

      assertGrpcError(
        sender.grpc.exchange(matcher,buy, sell, amount, price, matcherFee, matcherFee, matcherFee, ts, tver),
        "Assets should be issued before they can be traded",
        Code.INVALID_ARGUMENT
      )
    }
  }
}
