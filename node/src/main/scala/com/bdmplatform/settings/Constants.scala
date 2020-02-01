package com.bdmplatform.settings

import com.bdmplatform.Version
import com.bdmplatform.transaction.TransactionParsers
import com.bdmplatform.utils.ScorexLogging

/**
  * System constants here.
  */
object Constants extends ScorexLogging {
  val ApplicationName = "bdm"
  val AgentName       = s"Bdm v${Version.VersionString}"

  val UnitsInWave = 100000000L
  val TotalBdm  = 100000000L

  lazy val TransactionNames: Map[Byte, String] =
    TransactionParsers.all.map {
      case ((typeId, _), builder) =>
        val txName =
          builder.getClass.getSimpleName.init
            .replace("V1", "")
            .replace("V2", "")

        typeId -> txName
    }
}
