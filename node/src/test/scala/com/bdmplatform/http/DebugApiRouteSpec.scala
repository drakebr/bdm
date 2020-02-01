package com.bdmplatform.http

import com.bdmplatform.api.http.ApiError.ApiKeyNotValid
import com.bdmplatform.settings.BdmSettings
import com.bdmplatform.{NTPTime, TestWallet}

//noinspection ScalaStyle
class DebugApiRouteSpec extends RouteSpec("/debug") with RestAPISettingsHelper with TestWallet with NTPTime {

  private val sampleConfig  = com.typesafe.config.ConfigFactory.load()
  private val bdmSettings = BdmSettings.fromRootConfig(sampleConfig)
  private val configObject  = sampleConfig.root()
  private val route =
    DebugApiRoute(bdmSettings, ntpTime, null, null, null, null, null, null, null, null, null, null, null, null, configObject, _ => Seq.empty).route

  routePath("/configInfo") - {
    "requires api-key header" in {
      Get(routePath("/configInfo?full=true")) ~> route should produce(ApiKeyNotValid)
      Get(routePath("/configInfo?full=false")) ~> route should produce(ApiKeyNotValid)
    }
  }
}
