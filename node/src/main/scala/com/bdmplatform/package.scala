package com

import com.bdmplatform.block.Block
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.settings.BdmSettings
import com.bdmplatform.state.NG
import com.bdmplatform.transaction.TxValidationError.GenericError
import com.bdmplatform.transaction.BlockchainUpdater
import com.bdmplatform.utils.ScorexLogging

package object bdmplatform extends ScorexLogging {
  private def checkOrAppend(block: Block, blockchainUpdater: BlockchainUpdater with NG): Either[ValidationError, Unit] = {
    if (blockchainUpdater.isEmpty) {
      blockchainUpdater.processBlock(block).right.map { _ =>
        log.info(s"Genesis block ${blockchainUpdater.blockHeaderAndSize(1).get._1} has been added to the state")
      }
    } else {
      val existingGenesisBlockId: Option[ByteStr] = blockchainUpdater.blockHeaderAndSize(1).map(_._1.signerData.signature)
      Either.cond(existingGenesisBlockId.fold(false)(_ == block.uniqueId),
                  (),
                  GenericError("Mismatched genesis blocks in configuration and blockchain"))
    }
  }

  def checkGenesis(settings: BdmSettings, blockchainUpdater: BlockchainUpdater with NG): Unit = {
    Block
      .genesis(settings.blockchainSettings.genesisSettings)
      .flatMap { genesis =>
        log.debug(s"Genesis block: $genesis")
        log.debug(s"Genesis block json: ${genesis.json()}")
        checkOrAppend(genesis, blockchainUpdater)
      }
      .left
      .foreach { e =>
        log.error("INCORRECT NODE CONFIGURATION!!! NODE STOPPED BECAUSE OF THE FOLLOWING ERROR:")
        log.error(e.toString)
        com.bdmplatform.utils.forceStopApplication()
      }
  }
}
