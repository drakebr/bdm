package com.bdmplatform.transaction

trait VersionedTransaction {
  def version: Byte
}
