package com.bdmplatform.it.sync.grpc

import com.bdmplatform.it.NTPTime
import com.bdmplatform.it.sync._
import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.protobuf.transaction.PBTransactions
import com.bdmplatform.it.util._
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.protobuf.transaction.Recipient
import io.grpc.Status.Code

class ReissueTransactionGrpcSuite extends GrpcBaseTransactionSuite with NTPTime {

  val (reissuer, reissuerAddress) = (firstAcc, firstAddress)

  test("asset reissue changes issuer's asset balance; issuer's bdm balance is decreased by fee") {
    for (v <- supportedVersions) {
      val reissuerBalance = sender.grpc.bdmBalance(reissuerAddress).available
      val reissuerEffBalance = sender.grpc.bdmBalance(reissuerAddress).effective

      val issuedAssetTx = sender.grpc.broadcastIssue(reissuer, "assetname", someAssetAmount, decimals = 2, reissuable = true, issueFee, waitForTx = true)
      val issuedAssetId = PBTransactions.vanilla(issuedAssetTx).explicitGet().id().base58

      sender.grpc.broadcastReissue(reissuer, reissueFee, issuedAssetId, someAssetAmount, reissuable = true, version = v, waitForTx = true)

      sender.grpc.bdmBalance(reissuerAddress).available shouldBe reissuerBalance - issueFee - reissueFee
      sender.grpc.bdmBalance(reissuerAddress).effective shouldBe reissuerEffBalance - issueFee - reissueFee
      sender.grpc.assetsBalance(reissuerAddress, Seq(issuedAssetId)).getOrElse(issuedAssetId, 0L) shouldBe 2 * someAssetAmount
    }
  }

  test("can't reissue not reissuable asset") {
    for (v <- supportedVersions) {
      val reissuerBalance = sender.grpc.bdmBalance(reissuerAddress).available
      val reissuerEffBalance = sender.grpc.bdmBalance(reissuerAddress).effective

      val issuedAssetTx = sender.grpc.broadcastIssue(reissuer, "assetname", someAssetAmount, decimals = 2, reissuable = false, issueFee, waitForTx = true)
      val issuedAssetId = PBTransactions.vanilla(issuedAssetTx).explicitGet().id().base58

      assertGrpcError(sender.grpc.broadcastReissue(reissuer, reissueFee, issuedAssetId, someAssetAmount, version = v, reissuable = true, waitForTx = true),
        "Asset is not reissuable",
        Code.INVALID_ARGUMENT)

      sender.grpc.bdmBalance(reissuerAddress).available shouldBe reissuerBalance - issueFee
      sender.grpc.bdmBalance(reissuerAddress).effective shouldBe reissuerEffBalance - issueFee
      sender.grpc.assetsBalance(reissuerAddress, Seq(issuedAssetId)).getOrElse(issuedAssetId, 0L) shouldBe someAssetAmount
    }
  }

  test("not able to reissue if cannot pay fee - insufficient funds") {
    for (v <- supportedVersions) {
      val reissuerBalance = sender.grpc.bdmBalance(reissuerAddress).available
      val reissuerEffBalance = sender.grpc.bdmBalance(reissuerAddress).effective
      val hugeReissueFee = reissuerEffBalance + 1.bdm

      val issuedAssetTx = sender.grpc.broadcastIssue(reissuer, "assetname", someAssetAmount, decimals = 2, reissuable = true, issueFee, waitForTx = true)
      val issuedAssetId = PBTransactions.vanilla(issuedAssetTx).explicitGet().id().base58

      assertGrpcError(sender.grpc.broadcastReissue(reissuer, hugeReissueFee, issuedAssetId, someAssetAmount, reissuable = true, version = v, waitForTx = true),
        "negative bdm balance",
        Code.INVALID_ARGUMENT)

      sender.grpc.bdmBalance(reissuerAddress).available shouldBe reissuerBalance - issueFee
      sender.grpc.bdmBalance(reissuerAddress).effective shouldBe reissuerEffBalance - issueFee
      sender.grpc.assetsBalance(reissuerAddress, Seq(issuedAssetId)).getOrElse(issuedAssetId, 0L) shouldBe someAssetAmount
    }
  }

  test("asset becomes non-reissuable after reissue with reissuable=false") {
    for (v <- supportedVersions) {
      val reissuerBalance = sender.grpc.bdmBalance(reissuerAddress).available
      val reissuerEffBalance = sender.grpc.bdmBalance(reissuerAddress).effective

      val issuedAssetTx = sender.grpc.broadcastIssue(reissuer, "assetname", someAssetAmount, decimals = 2, reissuable = true, issueFee, waitForTx = true)
      val issuedAssetId = PBTransactions.vanilla(issuedAssetTx).explicitGet().id().base58

      sender.grpc.broadcastReissue(reissuer, reissueFee, issuedAssetId, someAssetAmount, reissuable = false, version = v, waitForTx = true)

      assertGrpcError(sender.grpc.broadcastReissue(reissuer, reissueFee, issuedAssetId, someAssetAmount, reissuable = true, version = v, waitForTx = true),
        "Asset is not reissuable",
        Code.INVALID_ARGUMENT)

      sender.grpc.bdmBalance(reissuerAddress).available shouldBe reissuerBalance - issueFee - reissueFee
      sender.grpc.bdmBalance(reissuerAddress).effective shouldBe reissuerEffBalance - issueFee - reissueFee
      sender.grpc.assetsBalance(reissuerAddress, Seq(issuedAssetId)).getOrElse(issuedAssetId, 0L) shouldBe 2 * someAssetAmount
    }
  }

  test("able to transfer new reissued amount of assets") {
    for (v <- supportedVersions) {
      val reissuerBalance = sender.grpc.bdmBalance(reissuerAddress).available
      val reissuerEffBalance = sender.grpc.bdmBalance(reissuerAddress).effective

      val issuedAssetTx = sender.grpc.broadcastIssue(reissuer, "assetname", someAssetAmount, decimals = 2, reissuable = true, issueFee, waitForTx = true)
      val issuedAssetId = PBTransactions.vanilla(issuedAssetTx).explicitGet().id().base58

      sender.grpc.broadcastReissue(reissuer, reissueFee, issuedAssetId, someAssetAmount, reissuable = true, version = v, waitForTx = true)

      sender.grpc.broadcastTransfer(reissuer, Recipient().withAddress(secondAddress), 2 * someAssetAmount, minFee, assetId = issuedAssetId, waitForTx = true)
      sender.grpc.bdmBalance(reissuerAddress).available shouldBe reissuerBalance - issueFee - reissueFee - minFee
      sender.grpc.bdmBalance(reissuerAddress).effective shouldBe reissuerEffBalance - issueFee - reissueFee - minFee
      sender.grpc.assetsBalance(reissuerAddress, Seq(issuedAssetId)).getOrElse(issuedAssetId, 0L) shouldBe 0L
      sender.grpc.assetsBalance(secondAddress, Seq(issuedAssetId)).getOrElse(issuedAssetId, 0L) shouldBe 2 * someAssetAmount
    }
  }


}
