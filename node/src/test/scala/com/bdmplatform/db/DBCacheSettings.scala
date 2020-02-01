package com.bdmplatform.db
import com.typesafe.config.ConfigFactory
import com.bdmplatform.settings.BdmSettings

trait DBCacheSettings {
  lazy val dbSettings = BdmSettings.fromRootConfig(ConfigFactory.load()).dbSettings
  lazy val maxCacheSize: Int = dbSettings.maxCacheSize
}
