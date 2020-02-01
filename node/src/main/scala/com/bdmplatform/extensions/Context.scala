package com.bdmplatform.extensions

import akka.actor.ActorSystem
import com.bdmplatform.account.Address
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.settings.BdmSettings
import com.bdmplatform.state.Blockchain
import com.bdmplatform.transaction.smart.script.trace.TracedResult
import com.bdmplatform.transaction.{Asset, Transaction}
import com.bdmplatform.utils.Time
import com.bdmplatform.utx.UtxPool
import com.bdmplatform.wallet.Wallet
import monix.reactive.Observable

trait Context {
  def settings: BdmSettings
  def blockchain: Blockchain
  def time: Time
  def wallet: Wallet
  def utx: UtxPool

  def broadcastTransaction(tx: Transaction): TracedResult[ValidationError, Boolean]
  def spendableBalanceChanged: Observable[(Address, Asset)]
  def actorSystem: ActorSystem
}
