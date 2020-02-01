package com.bdmplatform.api.common

import com.bdmplatform.account.Address
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.protobuf.transaction.VanillaTransaction
import com.bdmplatform.state.diffs.FeeValidation
import com.bdmplatform.state.diffs.FeeValidation.FeeDetails
import com.bdmplatform.state.{Blockchain, Height}
import com.bdmplatform.transaction.Asset
import com.bdmplatform.transaction.smart.script.trace.TracedResult
import com.bdmplatform.utx.UtxPool
import com.bdmplatform.wallet.Wallet
import monix.reactive.Observable

private[api] class CommonTransactionsApi(
    blockchain: Blockchain,
    utx: UtxPool,
    wallet: Wallet,
    publishTransaction: VanillaTransaction => TracedResult[ValidationError, Boolean]
) {
  def transactionsByAddress(address: Address, fromId: Option[ByteStr] = None): Observable[(Height, VanillaTransaction)] =
    blockchain.addressTransactionsObservable(address, Set.empty, fromId)

  def transactionById(transactionId: ByteStr): Option[(Int, VanillaTransaction)] =
    blockchain.transactionInfo(transactionId)

  def unconfirmedTransactions(): Seq[VanillaTransaction] =
    utx.all

  def unconfirmedTransactionById(transactionId: ByteStr): Option[VanillaTransaction] =
    utx.transactionById(transactionId)

  def calculateFee(tx: VanillaTransaction): Either[ValidationError, (Asset, Long, Long)] =
    FeeValidation
      .getMinFee(blockchain, blockchain.height, tx)
      .map {
        case FeeDetails(asset, _, feeInAsset, feeInBdm) =>
          (asset, feeInAsset, feeInBdm)
      }

  def broadcastTransaction(tx: VanillaTransaction): TracedResult[ValidationError, Boolean] = publishTransaction(tx)
}
