package com.bdmplatform.transaction
import com.bdmplatform.account.PublicKey

trait Authorized {
  val sender: PublicKey
}
