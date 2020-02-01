package com.bdmplatform.transaction

import cats.kernel.Monoid
import com.bdmplatform.account.PublicKey
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.lang.directives.DirectiveSet
import com.bdmplatform.lang.directives.values._
import com.bdmplatform.lang.script.ContractScript
import com.bdmplatform.lang.v1.compiler
import com.bdmplatform.lang.v1.evaluator.ctx.impl.bdm.BdmContext
import com.bdmplatform.lang.v1.evaluator.ctx.impl.{CryptoContext, PureContext}
import com.bdmplatform.lang.v1.parser.Parser
import com.bdmplatform.lang.v1.traits.Environment
import com.bdmplatform.lang.{Global, utils}
import com.bdmplatform.state.HistoryTest
import com.bdmplatform.transaction.assets.IssueTransactionV2
import com.bdmplatform.{TransactionGen, WithDB}
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.{ScalaCheckPropertyChecks => PropertyChecks}
import play.api.libs.json.Json

class IssueTransactionV2Specification extends PropSpec with PropertyChecks with Matchers with TransactionGen with WithDB with HistoryTest {

  property("SmartIssueTransaction serialization roundtrip") {
    forAll(smartIssueTransactionGen()) { tx: IssueTransactionV2 =>
      val recovered = IssueTransactionV2.parseBytes(tx.bytes()).get

      tx.sender.stringRepr shouldEqual recovered.sender.stringRepr
      tx.timestamp shouldEqual recovered.timestamp
      tx.decimals shouldEqual recovered.decimals
      tx.description shouldEqual recovered.description
      tx.script shouldEqual recovered.script
      tx.reissuable shouldEqual recovered.reissuable
      tx.fee shouldEqual recovered.fee
      tx.name shouldEqual recovered.name
      tx.chainId shouldEqual recovered.chainId
      tx.bytes() shouldEqual recovered.bytes()
    }
  }

  property("JSON format validation") {
    val js = Json.parse("""{
                       "type": 3,
                       "id": "2ykNAo5JrvNCcL8PtCmc9pTcNtKUy2PjJkrFdRvTfUf4",
                       "sender": "3N5GRqzDBhjVXnCn44baHcz2GoZy5qLxtTh",
                       "senderPublicKey": "FM5ojNqW7e9cZ9zhPYGkpSP1Pcd8Z3e3MNKYVS5pGJ8Z",
                       "fee": 100000000,
                       "feeAssetId": null,
                       "timestamp": 1526287561757,
                       "proofs": [
                       "43TCfWBa6t2o2ggsD4bU9FpvH3kmDbSBWKE1Z6B5i5Ax5wJaGT2zAvBihSbnSS3AikZLcicVWhUk1bQAMWVzTG5g"
                       ],
                       "version": 2,
                       "assetId": "2ykNAo5JrvNCcL8PtCmc9pTcNtKUy2PjJkrFdRvTfUf4",
                       "chainId": 84,
                       "name": "Gigacoin",
                       "quantity": 10000000000,
                       "reissuable": true,
                       "decimals": 8,
                       "description": "Gigacoin",
                       "script":null
                       }
    """)

    val tx = IssueTransactionV2
      .create(
        'T',
        PublicKey.fromBase58String("FM5ojNqW7e9cZ9zhPYGkpSP1Pcd8Z3e3MNKYVS5pGJ8Z").explicitGet(),
        "Gigacoin".getBytes("UTF-8"),
        "Gigacoin".getBytes("UTF-8"),
        10000000000L,
        8,
        true,
        None,
        100000000,
        1526287561757L,
        Proofs(Seq(ByteStr.decodeBase58("43TCfWBa6t2o2ggsD4bU9FpvH3kmDbSBWKE1Z6B5i5Ax5wJaGT2zAvBihSbnSS3AikZLcicVWhUk1bQAMWVzTG5g").get))
      )
      .right
      .get

    tx.json() shouldEqual js
  }

  property("Contract script on asset isn't allowed") {
    val contract = {
      val script =
        s"""
          |{-# STDLIB_VERSION 3 #-}
          |{-# CONTENT_TYPE CONTRACT #-}
          |
          |@Verifier(txx)
          |func verify() = {
          |    true
          |}
        """.stripMargin
      Parser.parseContract(script).get.value
    }

    val ctx = {
      utils.functionCosts(V3)
      Monoid
        .combineAll(
          Seq(
            PureContext.build(Global, V3).withEnvironment[Environment],
            CryptoContext.build(Global, V3).withEnvironment[Environment],
            BdmContext.build(
              DirectiveSet(V3, Account, Expression).explicitGet()
            )
          ))
    }

    val script = ContractScript(V3, compiler.ContractCompiler(ctx.compilerContext, contract).explicitGet())

    val tx = IssueTransactionV2
      .create(
        'T',
        PublicKey.fromBase58String("FM5ojNqW7e9cZ9zhPYGkpSP1Pcd8Z3e3MNKYVS5pGJ8Z").explicitGet(),
        "Gigacoin".getBytes("UTF-8"),
        "Gigacoin".getBytes("UTF-8"),
        10000000000L,
        8,
        true,
        script.toOption,
        100000000,
        1526287561757L,
        Proofs(Seq(ByteStr.decodeBase58("43TCfWBa6t2o2ggsD4bU9FpvH3kmDbSBWKE1Z6B5i5Ax5wJaGT2zAvBihSbnSS3AikZLcicVWhUk1bQAMWVzTG5g").get))
      )

    tx shouldBe 'left
  }
}
