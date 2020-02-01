package com.bdmplatform.state.diffs

import com.bdmplatform.lang.ValidationError
import com.bdmplatform.state.{Blockchain, Diff, LeaseBalance, Portfolio}
import com.bdmplatform.transaction.smart.SetScriptTransaction

object SetScriptTransactionDiff {
  def apply(blockchain: Blockchain, height: Int)(tx: SetScriptTransaction): Either[ValidationError, Diff] =
    DiffsCommon.countVerifierComplexity(tx.script, blockchain)
      .map(
        script => Diff(
          height = height,
          tx = tx,
          portfolios = Map(tx.sender.toAddress -> Portfolio(-tx.fee, LeaseBalance.empty, Map.empty)),
          scripts = Map(tx.sender.toAddress -> script),
          scriptsRun = DiffsCommon.countScriptRuns(blockchain, tx)
        )
      )
}
