package com.bdmplatform.state

import com.bdmplatform.block.Block.BlockId
import com.bdmplatform.block.MicroBlock
import com.bdmplatform.common.state.ByteStr

trait NG extends Blockchain {
  def microBlock(id: ByteStr): Option[MicroBlock]

  def bestLastBlockInfo(maxTimestamp: Long): Option[BlockMinerInfo]

  def lastPersistedBlockIds(count: Int): Seq[BlockId]

  def microblockIds: Seq[BlockId]
}
