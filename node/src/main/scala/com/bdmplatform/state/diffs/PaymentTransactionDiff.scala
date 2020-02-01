package com.bdmplatform.state.diffs

import cats.implicits._
import com.bdmplatform.account.Address
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.settings.FunctionalitySettings
import com.bdmplatform.state.{Diff, LeaseBalance, Portfolio}
import com.bdmplatform.transaction.PaymentTransaction
import com.bdmplatform.transaction.TxValidationError.GenericError

import scala.util.{Left, Right}

object PaymentTransactionDiff {

  def apply(settings: FunctionalitySettings, height: Int, blockTime: Long)(tx: PaymentTransaction): Either[ValidationError, Diff] = {

    if (height > settings.blockVersion3AfterHeight) {
      Left(GenericError(s"Payment transaction is deprecated after h=${settings.blockVersion3AfterHeight}"))
    } else {
      Right(
        Diff(
          height = height,
          tx = tx,
          portfolios = Map(tx.recipient -> Portfolio(balance = tx.amount, LeaseBalance.empty, assets = Map.empty)) combine Map(
            Address.fromPublicKey(tx.sender) -> Portfolio(
              balance = -tx.amount - tx.fee,
              LeaseBalance.empty,
              assets = Map.empty
            ))
        ))
    }
  }
}
