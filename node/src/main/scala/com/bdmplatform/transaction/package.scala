package com.bdmplatform

import com.bdmplatform.block.{Block, MicroBlock}
import com.bdmplatform.utils.base58Length

package object transaction {
  val AssetIdLength: Int       = com.bdmplatform.crypto.DigestSize
  val AssetIdStringLength: Int = base58Length(AssetIdLength)
  type DiscardedTransactions = Seq[Transaction]
  type DiscardedBlocks       = Seq[Block]
  type DiscardedMicroBlocks  = Seq[MicroBlock]
  type AuthorizedTransaction = Authorized with Transaction
}
