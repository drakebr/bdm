package com.bdmplatform.it

import com.typesafe.config.ConfigFactory.{defaultApplication, defaultReference}
import com.bdmplatform.account.PublicKey
import com.bdmplatform.block.Block
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.consensus.PoSSelector
import com.bdmplatform.database.openDB
import com.bdmplatform.history.StorageFactory
import com.bdmplatform.settings._
import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.utils.NTP
import monix.execution.UncaughtExceptionReporter
import monix.reactive.Observer
import net.ceedubs.ficus.Ficus._

object BaseTargetChecker {
  def main(args: Array[String]): Unit = {
    val sharedConfig = Docker.genesisOverride
      .withFallback(Docker.configTemplate)
      .withFallback(defaultApplication())
      .withFallback(defaultReference())
      .resolve()

    val settings          = BdmSettings.fromRootConfig(sharedConfig)
    val db                = openDB("/tmp/tmp-db")
    val ntpTime           = new NTP("ntp.pool.org")
    val portfolioChanges  = Observer.empty(UncaughtExceptionReporter.default)
    val blockchainUpdater = StorageFactory(settings, db, ntpTime, portfolioChanges)
    val poSSelector       = new PoSSelector(blockchainUpdater, settings.blockchainSettings, settings.synchronizationSettings)

    try {
      val genesisBlock = Block.genesis(settings.blockchainSettings.genesisSettings).explicitGet()
      blockchainUpdater.processBlock(genesisBlock)

      NodeConfigs.Default.map(_.withFallback(sharedConfig)).collect {
        case cfg if cfg.as[Boolean]("bdm.miner.enable") =>
          val account   = PublicKey(cfg.as[ByteStr]("public-key").arr)
          val address   = account.toAddress
          val balance   = blockchainUpdater.balance(address, Bdm)
          val consensus = genesisBlock.consensusData
          val timeDelay = poSSelector
            .getValidBlockDelay(blockchainUpdater.height, account, consensus.baseTarget, balance)
            .explicitGet()

          f"$address: ${timeDelay * 1e-3}%10.3f s"
      }
    } finally ntpTime.close()
  }
}
