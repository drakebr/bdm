package com.bdmplatform.state.diffs

import com.bdmplatform.features.BlockchainFeatures
import com.bdmplatform.features.FeatureProvider._
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.state.{Blockchain, Diff, LeaseBalance, Portfolio}
import com.bdmplatform.transaction.CreateAliasTransaction
import com.bdmplatform.transaction.TxValidationError.GenericError

import scala.util.Right

object CreateAliasTransactionDiff {
  def apply(blockchain: Blockchain, height: Int)(tx: CreateAliasTransaction): Either[ValidationError, Diff] =
    if (blockchain.isFeatureActivated(BlockchainFeatures.DataTransaction, height) && !blockchain.canCreateAlias(tx.alias))
      Left(GenericError("Alias already claimed"))
    else
      Right(
        Diff(
          height = height,
          tx = tx,
          portfolios = Map(tx.sender.toAddress -> Portfolio(-tx.fee, LeaseBalance.empty, Map.empty)),
          aliases = Map(tx.alias -> tx.sender.toAddress),
          scriptsRun = DiffsCommon.countScriptRuns(blockchain, tx)
        )
      )
}
