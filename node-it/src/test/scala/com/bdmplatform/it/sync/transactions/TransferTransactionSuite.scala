package com.bdmplatform.it.sync.transactions

import com.bdmplatform.account.AddressOrAlias
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.it.sync._
import com.bdmplatform.it.transactions.BaseTransactionSuite
import com.bdmplatform.it.util._
import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.transaction.transfer._
import org.scalatest.CancelAfterFailure

import scala.concurrent.duration._

class TransferTransactionSuite extends BaseTransactionSuite with CancelAfterFailure {

  test("asset transfer changes sender's and recipient's asset balance; issuer's.bdm balance is decreased by fee") {
    for (v <- supportedVersions) {
      val (firstBalance, firstEffBalance)   = miner.accountBalances(firstAddress)
      val (secondBalance, secondEffBalance) = miner.accountBalances(secondAddress)

      val issuedAssetId = sender.issue(firstAddress, "name", "description", someAssetAmount, 2, reissuable = false, issueFee).id

      nodes.waitForHeightAriseAndTxPresent(issuedAssetId)

      miner.assertBalances(firstAddress, firstBalance - issueFee, firstEffBalance - issueFee)
      miner.assertAssetBalance(firstAddress, issuedAssetId, someAssetAmount)

      val transferTransactionId = sender.transfer(firstAddress, secondAddress, someAssetAmount, minFee, Some(issuedAssetId), version = v).id
      nodes.waitForHeightAriseAndTxPresent(transferTransactionId)

      miner.assertBalances(firstAddress, firstBalance - minFee - issueFee, firstEffBalance - minFee - issueFee)
      miner.assertBalances(secondAddress, secondBalance, secondEffBalance)
      miner.assertAssetBalance(firstAddress, issuedAssetId, 0)
      miner.assertAssetBalance(secondAddress, issuedAssetId, someAssetAmount)
    }
  }

  test("bdm transfer changes bdm balances and eff.b.") {
    for (v <- supportedVersions) {
      val (firstBalance, firstEffBalance)   = miner.accountBalances(firstAddress)
      val (secondBalance, secondEffBalance) = miner.accountBalances(secondAddress)

      val transferId = sender.transfer(firstAddress, secondAddress, transferAmount, minFee, version = v).id

      nodes.waitForHeightAriseAndTxPresent(transferId)

      miner.assertBalances(firstAddress, firstBalance - transferAmount - minFee, firstEffBalance - transferAmount - minFee)
      miner.assertBalances(secondAddress, secondBalance + transferAmount, secondEffBalance + transferAmount)
    }
  }

  test("invalid signed bdm transfer should not be in UTX or blockchain") {
    def invalidTx(timestamp: Long = System.currentTimeMillis, fee: Long = 100000): TransferTransactionV1.TransactionT =
      TransferTransactionV1
        .selfSigned(Bdm, sender.privateKey, AddressOrAlias.fromString(sender.address).explicitGet(), 1, timestamp, Bdm, fee, Array.emptyByteArray)
        .right
        .get

    val (balance1, eff1) = miner.accountBalances(firstAddress)

    val invalidTxs = Seq(
      (invalidTx(timestamp = System.currentTimeMillis + 1.day.toMillis), "Transaction timestamp .* is more than .*ms in the future"),
      (invalidTx(fee = 99999), "Fee .* does not exceed minimal value")
    )

    for ((tx, diag) <- invalidTxs) {
      assertBadRequestAndResponse(sender.broadcastRequest(tx.json()), diag)
      nodes.foreach(_.ensureTxDoesntExist(tx.id().base58))
    }

    nodes.waitForHeightArise()
    miner.assertBalances(firstAddress, balance1, eff1)

  }

  test("can not make transfer without having enough effective balance") {
    for (v <- supportedVersions) {
      val (secondBalance, secondEffBalance) = miner.accountBalances(secondAddress)

      assertApiErrorRaised(sender.transfer(secondAddress, firstAddress, secondEffBalance, minFee, version = v))
      nodes.waitForHeightArise()

      miner.assertBalances(secondAddress, secondBalance, secondEffBalance)
    }
  }

  test("can not make transfer without having enough balance") {
    for (v <- supportedVersions) {
      val (secondBalance, secondEffBalance) = miner.accountBalances(secondAddress)

      assertBadRequestAndResponse(sender.transfer(secondAddress, firstAddress, secondBalance + 1.bdm, minFee, version = v),
                                  "Attempt to transfer unavailable funds")
      miner.assertBalances(secondAddress, secondBalance, secondEffBalance)
    }
  }

  test("can forge block with sending majority of some asset to self and to other account") {
    for (v <- supportedVersions) {
      val (firstBalance, firstEffBalance)   = miner.accountBalances(firstAddress)
      val (secondBalance, secondEffBalance) = miner.accountBalances(secondAddress)

      val assetId = sender.issue(firstAddress, "second asset", "description", someAssetAmount, 0, reissuable = false, fee = issueFee).id

      nodes.waitForHeightAriseAndTxPresent(assetId)

      miner.assertBalances(firstAddress, firstBalance - issueFee, firstEffBalance - issueFee)
      miner.assertAssetBalance(firstAddress, assetId, someAssetAmount)

      val tx1 = sender.transfer(firstAddress, firstAddress, someAssetAmount, minFee, Some(assetId), version = v).id
      nodes.waitForHeightAriseAndTxPresent(tx1)

      val tx2 = sender.transfer(firstAddress, secondAddress, someAssetAmount / 2, minFee, Some(assetId), version = v).id
      nodes.waitForHeightAriseAndTxPresent(tx2)

      miner.assertBalances(firstAddress, firstBalance - issueFee - 2 * minFee, firstEffBalance - issueFee - 2 * minFee)
      miner.assertBalances(secondAddress, secondBalance, secondEffBalance)
    }
  }
}
