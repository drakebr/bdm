package com.bdmplatform.db

import java.nio.file.Files

import com.bdmplatform.database.{LevelDBWriter, openDB}
import com.bdmplatform.history.Domain
import com.bdmplatform.settings.{BlockchainSettings, FunctionalitySettings, BdmSettings, loadConfig}
import com.bdmplatform.state.BlockchainUpdaterImpl
import com.bdmplatform.state.utils.TestLevelDB
import com.bdmplatform.{NTPTime, TestHelpers}
import monix.reactive.Observer
import org.scalatest.Suite

trait WithState extends DBCacheSettings {
  def withLevelDBWriter[A](bs: BlockchainSettings)(test: LevelDBWriter => A): A = {
    val path = Files.createTempDirectory("leveldb-test")
    val db   = openDB(path.toAbsolutePath.toString)
    try test(new LevelDBWriter(db, Observer.stopped, bs, dbSettings))
    finally {
      db.close()
      TestHelpers.deleteRecursively(path)
    }
  }

  def withLevelDBWriter[A](fs: FunctionalitySettings)(test: LevelDBWriter => A): A =
    withLevelDBWriter(TestLevelDB.createTestBlockchainSettings(fs))(test)
}

trait WithDomain extends WithState with NTPTime { _: Suite =>
  def withDomain[A](settings: BdmSettings = BdmSettings.fromRootConfig(loadConfig(None)))(test: Domain => A): A =
    withLevelDBWriter(settings.blockchainSettings) { blockchain =>
      val bcu = new BlockchainUpdaterImpl(blockchain, Observer.stopped, settings, ntpTime)
      try test(Domain(bcu, blockchain))
      finally bcu.shutdown()
    }
}
