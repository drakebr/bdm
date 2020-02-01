package com.bdmplatform.it.sync.debug

import com.typesafe.config.Config
import com.bdmplatform.it.{Node, NodeConfigs}
import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.it.transactions.NodesFromDocker
import com.bdmplatform.it.util._
import com.bdmplatform.it.sync._
import org.scalatest.FunSuite

class DebugPortfoliosSuite extends FunSuite with NodesFromDocker {
  override protected def nodeConfigs: Seq[Config] =
    NodeConfigs.newBuilder
      .overrideBase(_.quorum(0))
      .withDefault(entitiesNumber = 1)
      .buildNonConflicting()

  private def sender: Node = nodes.head

  private val firstAddress  = sender.createAddress()
  private val secondAddress = sender.createAddress()

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    sender.transfer(sender.address, firstAddress, 20.bdm, minFee, waitForTx = true)
    sender.transfer(sender.address, secondAddress, 20.bdm, minFee, waitForTx = true)
  }

  test("getting a balance considering pessimistic transactions from UTX pool - changed after UTX") {
    val portfolioBefore = sender.debugPortfoliosFor(firstAddress, considerUnspent = true)
    val utxSizeBefore   = sender.utxSize

    sender.transfer(firstAddress, secondAddress, 5.bdm, 5.bdm)
    sender.transfer(secondAddress, firstAddress, 7.bdm, 5.bdm)

    sender.waitForUtxIncreased(utxSizeBefore)

    val portfolioAfter = sender.debugPortfoliosFor(firstAddress, considerUnspent = true)

    val expectedBalance = portfolioBefore.balance - 10.bdm // withdraw + fee
    assert(portfolioAfter.balance == expectedBalance)

  }

  test("getting a balance without pessimistic transactions from UTX pool - not changed after UTX") {
    nodes.waitForHeightArise()

    val portfolioBefore = sender.debugPortfoliosFor(firstAddress, considerUnspent = false)
    val utxSizeBefore   = sender.utxSize

    sender.transfer(firstAddress, secondAddress, 5.bdm, fee = 5.bdm)
    sender.waitForUtxIncreased(utxSizeBefore)

    val portfolioAfter = sender.debugPortfoliosFor(firstAddress, considerUnspent = false)
    assert(portfolioAfter.balance == portfolioBefore.balance)
  }
}
