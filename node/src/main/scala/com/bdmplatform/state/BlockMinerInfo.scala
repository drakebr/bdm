package com.bdmplatform.state

import com.bdmplatform.block.Block.BlockId
import com.bdmplatform.consensus.nxt.NxtLikeConsensusBlockData

case class BlockMinerInfo(consensus: NxtLikeConsensusBlockData, timestamp: Long, blockId: BlockId)
