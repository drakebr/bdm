package com.bdmplatform.state.diffs

import com.bdmplatform.features.EstimatorProvider._
import com.bdmplatform.lang.contract.DApp
import com.bdmplatform.lang.script.Script
import com.bdmplatform.lang.v1.compiler.Terms.FUNCTION_CALL
import com.bdmplatform.lang.v1.evaluator.ContractEvaluator.DEFAULT_FUNC_NAME
import com.bdmplatform.state.Blockchain
import com.bdmplatform.transaction.ProvenTransaction
import cats.implicits._
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.lang.v1.estimator.{ScriptEstimator, ScriptEstimatorV1}
import com.bdmplatform.lang.v2.estimator.ScriptEstimatorV2
import com.bdmplatform.transaction.TxValidationError.GenericError

private[diffs] object DiffsCommon {
  def functionComplexity(
    script:    Script,
    estimator: ScriptEstimator,
    maybeCall: Option[FUNCTION_CALL]
  ): Either[String, Long] =
    Script.complexityInfo(script, estimator)
      .map(calcFunctionComplexity(script, maybeCall, _))

  private def calcFunctionComplexity(
    script:     Script,
    maybeCall:  Option[FUNCTION_CALL],
    complexity: (Long, Map[String, Long])
  ): Long = {
    val (totalComplexity, cm) = complexity
    maybeCall match {
      case Some(call) =>
        cm.getOrElse(call.function.funcName, totalComplexity)

      case None =>
        script.expr match {
          case DApp(_, _, callables, _) =>
            callables
              .find(f => (f.u.name == DEFAULT_FUNC_NAME) && f.u.args.isEmpty)
              .flatMap(f => cm.get(f.u.name))
              .getOrElse(totalComplexity)

          case _ => totalComplexity
        }
    }
  }

  def countScriptRuns(blockchain: Blockchain, tx: ProvenTransaction): Int =
    tx.checkedAssets().count(blockchain.hasAssetScript) + Some(tx.sender.toAddress).count(blockchain.hasScript)

  def getScriptsComplexity(blockchain: Blockchain, tx: ProvenTransaction): Long = {
    val assetsComplexity = tx
      .checkedAssets()
      .toList
      .flatMap(blockchain.assetScriptWithComplexity)
      .map(_._2)

    val accountComplexity = blockchain
      .accountScriptWithComplexity(tx.sender.toAddress)
      .map(_._2)

    assetsComplexity.sum + accountComplexity.getOrElse(0L)
  }

  def countVerifierComplexity(
    script: Option[Script],
    blockchain: Blockchain
  ): Either[ValidationError, Option[(Script, Long)]] =
    script
      .traverse { script =>
        val cost =
          if (blockchain.height > blockchain.settings.functionalitySettings.estimatorPreCheckHeight)
            Script.verifierComplexity(script, ScriptEstimatorV1) *>
            Script.verifierComplexity(script, ScriptEstimatorV2)
          else
            Script.verifierComplexity(script, blockchain.estimator)

        cost.map((script, _))
      }
      .leftMap(GenericError(_))
}
