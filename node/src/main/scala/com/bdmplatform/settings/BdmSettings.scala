package com.bdmplatform.settings

import com.typesafe.config.Config
import com.bdmplatform.metrics.Metrics
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

import scala.concurrent.duration.FiniteDuration

case class BdmSettings(directory: String,
                         ntpServer: String,
                         dbSettings: DBSettings,
                         extensions: Seq[String],
                         extensionsShutdownTimeout: FiniteDuration,
                         networkSettings: NetworkSettings,
                         walletSettings: WalletSettings,
                         blockchainSettings: BlockchainSettings,
                         minerSettings: MinerSettings,
                         restAPISettings: RestAPISettings,
                         synchronizationSettings: SynchronizationSettings,
                         utxSettings: UtxSettings,
                         featuresSettings: FeaturesSettings,
                         rewardsSettings: RewardsVotingSettings,
                         metrics: Metrics.Settings,
                         config: Config)

object BdmSettings extends CustomValueReaders {
  def fromRootConfig(rootConfig: Config): BdmSettings = {
    val bdm = rootConfig.getConfig("bdm")

    val directory                 = bdm.as[String]("directory")
    val ntpServer                 = bdm.as[String]("ntp-server")
    val dbSettings                = bdm.as[DBSettings]("db")
    val extensions                = bdm.as[Seq[String]]("extensions")
    val extensionsShutdownTimeout = bdm.as[FiniteDuration]("extensions-shutdown-timeout")
    val networkSettings           = bdm.as[NetworkSettings]("network")
    val walletSettings            = bdm.as[WalletSettings]("wallet")
    val blockchainSettings        = bdm.as[BlockchainSettings]("blockchain")
    val minerSettings             = bdm.as[MinerSettings]("miner")
    val restAPISettings           = bdm.as[RestAPISettings]("rest-api")
    val synchronizationSettings   = bdm.as[SynchronizationSettings]("synchronization")
    val utxSettings               = bdm.as[UtxSettings]("utx")
    val featuresSettings          = bdm.as[FeaturesSettings]("features")
    val rewardsSettings           = bdm.as[RewardsVotingSettings]("rewards")
    val metrics                   = rootConfig.as[Metrics.Settings]("metrics") // TODO: Move to bdm section

    BdmSettings(
      directory,
      ntpServer,
      dbSettings,
      extensions,
      extensionsShutdownTimeout,
      networkSettings,
      walletSettings,
      blockchainSettings,
      minerSettings,
      restAPISettings,
      synchronizationSettings,
      utxSettings,
      featuresSettings,
      rewardsSettings,
      metrics,
      rootConfig
    )
  }
}
