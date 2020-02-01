package com.bdmplatform.protobuf

package object transaction {
  type PBOrder = com.bdmplatform.protobuf.order.Order
  val PBOrder = com.bdmplatform.protobuf.order.Order

  type VanillaOrder = com.bdmplatform.transaction.assets.exchange.Order
  val VanillaOrder = com.bdmplatform.transaction.assets.exchange.Order

  type PBTransaction = com.bdmplatform.protobuf.transaction.Transaction
  val PBTransaction = com.bdmplatform.protobuf.transaction.Transaction

  type PBSignedTransaction = com.bdmplatform.protobuf.transaction.SignedTransaction
  val PBSignedTransaction = com.bdmplatform.protobuf.transaction.SignedTransaction

  type VanillaTransaction = com.bdmplatform.transaction.Transaction
  val VanillaTransaction = com.bdmplatform.transaction.Transaction

  type VanillaSignedTransaction = com.bdmplatform.transaction.SignedTransaction

  type VanillaAssetId = com.bdmplatform.transaction.Asset
}
