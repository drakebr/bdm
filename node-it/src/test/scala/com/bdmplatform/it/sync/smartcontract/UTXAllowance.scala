package com.bdmplatform.it.sync.smartcontract

import com.typesafe.config.{Config, ConfigFactory}
import com.bdmplatform.account.KeyPair
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.it.sync._
import com.bdmplatform.it.transactions.NodesFromDocker
import com.bdmplatform.it.util._
import com.bdmplatform.it.{ReportingTestName, WaitForHeight2}
import com.bdmplatform.lang.v2.estimator.ScriptEstimatorV2
import com.bdmplatform.transaction.smart.script.ScriptCompiler
import org.scalatest.{CancelAfterFailure, FreeSpec, Matchers}

class UTXAllowance extends FreeSpec with Matchers with WaitForHeight2 with CancelAfterFailure with ReportingTestName with NodesFromDocker {
  import UTXAllowance._

  override protected def nodeConfigs: Seq[Config] = Configs

  private def nodeA = nodes.head
  private def nodeB = nodes.last

  "create two nodes with scripted accounts and check UTX" in {
    val accounts = List(nodeA, nodeB).map(i => {

      val nodeAddress = i.createAddress()
      val acc         = KeyPair.fromSeed(i.seed(nodeAddress)).right.get

      i.transfer(i.address, nodeAddress, 10.bdm, 0.005.bdm, None, waitForTx = true)

      val scriptText = s"""true""".stripMargin
      val script               = ScriptCompiler(scriptText, isAssetScript = false, ScriptEstimatorV2).explicitGet()._1.bytes().base64
      i.setScript(acc.stringRepr, Some(script), setScriptFee, waitForTx = true)

      acc
    })

    assertBadRequestAndMessage(
      nodeA
        .transfer(
          accounts.head.stringRepr,
          recipient = accounts.head.stringRepr,
          assetId = None,
          amount = 1.bdm,
          fee = minFee + 0.004.bdm,
          version = 2
        ),
      "transactions from scripted accounts are denied from UTX pool"
    )

    val txBId =
      nodeB
        .transfer(
          accounts(1).stringRepr,
          recipient = accounts(1).stringRepr,
          assetId = None,
          amount = 1.01.bdm,
          fee = minFee + 0.004.bdm,
          version = 2
        )
        .id

    nodes.waitForHeightArise()
    nodeA.findTransactionInfo(txBId) shouldBe None
  }

}

object UTXAllowance {
  import com.bdmplatform.it.NodeConfigs._
  private val FirstNode = ConfigFactory.parseString(s"""
                                                         |bdm {
                                                         |  utx.allow-transactions-from-smart-accounts = false
                                                         |  miner {
                                                         |      quorum = 0
                                                         |      enable = yes
                                                         |  }
                                                         |}""".stripMargin)

  private val SecondNode = ConfigFactory.parseString(s"""
                                                          |bdm {
                                                          |  utx.allow-transactions-from-smart-accounts = true
                                                          |  miner {
                                                          |      enable = no
                                                          |  }
                                                          |}""".stripMargin)

  val Configs: Seq[Config] = Seq(
    FirstNode.withFallback(Default.head),
    SecondNode.withFallback(Default(1))
  )

}
