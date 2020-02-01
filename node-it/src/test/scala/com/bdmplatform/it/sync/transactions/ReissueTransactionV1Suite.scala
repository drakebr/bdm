package com.bdmplatform.it.sync.transactions

import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.it.transactions.BaseTransactionSuite
import com.bdmplatform.it.util._
import com.bdmplatform.it.sync._

class ReissueTransactionV1Suite extends BaseTransactionSuite {

  test("asset reissue changes issuer's asset balance; issuer's bdm balance is decreased by fee") {

    val (balance, effectiveBalance) = miner.accountBalances(firstAddress)

    val issuedAssetId = sender.issue(firstAddress, "name2", "description2", someAssetAmount, decimals = 2, reissuable = true, fee = issueFee).id
    nodes.waitForHeightAriseAndTxPresent(issuedAssetId)
    miner.assertBalances(firstAddress, balance - issueFee, effectiveBalance - issueFee)
    miner.assertAssetBalance(firstAddress, issuedAssetId, someAssetAmount)

    val reissueTxId = sender.reissue(firstAddress, issuedAssetId, someAssetAmount, reissuable = true, fee = reissueFee).id
    nodes.waitForHeightAriseAndTxPresent(reissueTxId)
    miner.assertBalances(firstAddress, balance - issueFee - reissueFee, effectiveBalance - issueFee - reissueFee)
    miner.assertAssetBalance(firstAddress, issuedAssetId, 2 * someAssetAmount)
  }

  test("can't reissue not reissuable asset") {
    val (balance, effectiveBalance) = miner.accountBalances(firstAddress)

    val issuedAssetId = sender.issue(firstAddress, "name2", "description2", someAssetAmount, decimals = 2, reissuable = false, issueFee).id
    nodes.waitForHeightAriseAndTxPresent(issuedAssetId)
    miner.assertBalances(firstAddress, balance - issueFee, effectiveBalance - issueFee)
    miner.assertAssetBalance(firstAddress, issuedAssetId, someAssetAmount)

    assertBadRequestAndMessage(sender.reissue(firstAddress, issuedAssetId, someAssetAmount, reissuable = true, fee = reissueFee),
                               "Asset is not reissuable")
    nodes.waitForHeightArise()

    miner.assertAssetBalance(firstAddress, issuedAssetId, someAssetAmount)
    miner.assertBalances(firstAddress, balance - issueFee, effectiveBalance - issueFee)
  }

  test("not able to reissue if cannot pay fee - insufficient funds") {

    val (balance, effectiveBalance) = miner.accountBalances(firstAddress)
    val reissueFee                  = effectiveBalance + 1.bdm

    val issuedAssetId = sender.issue(firstAddress, "name3", "description3", someAssetAmount, decimals = 2, reissuable = true, issueFee).id

    nodes.waitForHeightAriseAndTxPresent(issuedAssetId)

    assertBadRequestAndMessage(sender.reissue(firstAddress, issuedAssetId, someAssetAmount, reissuable = true, fee = reissueFee),
                               "negative bdm balance")
    nodes.waitForHeightArise()

    miner.assertAssetBalance(firstAddress, issuedAssetId, someAssetAmount)
    miner.assertBalances(firstAddress, balance - issueFee, effectiveBalance - issueFee)

  }

}
