package com.bdmplatform.state.diffs.smart

import java.nio.charset.StandardCharsets

import com.bdmplatform.account.AddressScheme
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.lang.directives.values.{Expression, V3}
import com.bdmplatform.lang.script.v1.ExprScript
import com.bdmplatform.lang.utils._
import com.bdmplatform.lang.v1.compiler.ExpressionCompiler
import com.bdmplatform.lang.v1.parser.Parser
import com.bdmplatform.state.diffs._
import com.bdmplatform.transaction.Asset.{IssuedAsset, Bdm}
import com.bdmplatform.transaction.GenesisTransaction
import com.bdmplatform.transaction.assets.{IssueTransactionV2, SetAssetScriptTransaction}
import com.bdmplatform.transaction.transfer._
import com.bdmplatform.{NoShrink, TransactionGen}
import org.scalacheck.Gen
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.{ScalaCheckPropertyChecks => PropertyChecks}

class SmartAssetEvalTest extends PropSpec with PropertyChecks with Matchers with TransactionGen with NoShrink {
  val preconditions: Gen[(GenesisTransaction, IssueTransactionV2, SetAssetScriptTransaction, TransferTransactionV1)] =
    for {
      firstAcc  <- accountGen
      secondAcc <- accountGen
      ts        <- timestampGen
      genesis = GenesisTransaction.create(firstAcc, ENOUGH_AMT, ts).explicitGet()

      emptyScript = s"""
                       |{-# STDLIB_VERSION 3 #-}
                       |{-# CONTENT_TYPE EXPRESSION #-}
                       |{-# SCRIPT_TYPE ASSET #-}
                       |
                       |true
                       |
        """.stripMargin

      parsedEmptyScript = Parser.parseExpr(emptyScript).get.value

      emptyExprScript = ExprScript(
        V3,
        ExpressionCompiler(compilerContext(V3, Expression, isAssetScript = true), parsedEmptyScript).explicitGet()._1)
        .explicitGet()

      issueTransaction = IssueTransactionV2
        .selfSigned(
          AddressScheme.current.chainId,
          firstAcc,
          "name".getBytes(StandardCharsets.UTF_8),
          "description".getBytes(StandardCharsets.UTF_8),
          100,
          0,
          false,
          Some(emptyExprScript),
          1000000,
          ts
        )
        .explicitGet()

      asset = IssuedAsset(issueTransaction.id())

      assetScript = s"""
                       | {-# STDLIB_VERSION 3 #-}
                       | {-# CONTENT_TYPE EXPRESSION #-}
                       | {-# SCRIPT_TYPE ASSET #-}
                       |
                       | this.id         == base58'${asset.id.base58}' &&
                       | this.quantity   == 100                        &&
                       | this.decimals   == 0                          &&
                       | this.reissuable == false                      &&
                       | this.scripted   == true                       &&
                       | this.sponsored  == false
                       |
        """.stripMargin

      untypedScript = Parser.parseExpr(assetScript).get.value

      typedScript = ExprScript(V3,
                               ExpressionCompiler(compilerContext(V3, Expression, isAssetScript = true), untypedScript).explicitGet()._1)
        .explicitGet()

      setAssetScriptTransaction = SetAssetScriptTransaction
        .signed(AddressScheme.current.chainId, firstAcc, asset, Some(typedScript), 1000, ts + 10, firstAcc)
        .explicitGet()

      assetTransferTransaction = TransferTransactionV1
        .selfSigned(asset, firstAcc, secondAcc, 1, ts + 20, Bdm, 1000, Array.empty)
        .explicitGet()

    } yield (genesis, issueTransaction, setAssetScriptTransaction, assetTransferTransaction)

  property("Smart asset with scrtipt that contains 'this' link") {
    forAll(preconditions) {
      case (genesis, issueTransaction, setAssetScriptTransaction, assetTransferTransaction) =>
        assertDiffAndState(smartEnabledFS) { append =>
          append(Seq(genesis)).explicitGet()
          append(Seq(issueTransaction)).explicitGet()
          append(Seq(setAssetScriptTransaction)).explicitGet()
          append(Seq(assetTransferTransaction)) shouldBe 'right
        }
    }
  }
}
