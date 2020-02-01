package com.bdmplatform.features

import com.bdmplatform.state.Blockchain
import BlockchainFeatures.BlockReward
import FeatureProvider._
import com.bdmplatform.lang.v1.estimator.{ScriptEstimator, ScriptEstimatorV1}
import com.bdmplatform.lang.v2.estimator.ScriptEstimatorV2
import com.bdmplatform.settings.BdmSettings

object EstimatorProvider {
  implicit class EstimatorBlockchainExt(b: Blockchain) {
    val estimator: ScriptEstimator =
      if (b.isFeatureActivated(BlockReward)) ScriptEstimatorV2
      else ScriptEstimatorV1
  }

  implicit class EstimatorBdmSettingsExt(ws: BdmSettings) {
    val estimator: ScriptEstimator =
      if (ws.featuresSettings.supported.contains(BlockReward.id)) ScriptEstimatorV2
      else ScriptEstimatorV1
  }
}
