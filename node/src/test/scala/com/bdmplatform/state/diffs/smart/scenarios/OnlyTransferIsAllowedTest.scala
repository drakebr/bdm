package com.bdmplatform.state.diffs.smart.scenarios

import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.lagonaki.mocks.TestBlock
import com.bdmplatform.lang.directives.values._
import com.bdmplatform.lang.utils._
import com.bdmplatform.lang.v1.compiler.ExpressionCompiler
import com.bdmplatform.lang.v1.parser.Parser
import com.bdmplatform.state.diffs._
import com.bdmplatform.state.diffs.smart._
import com.bdmplatform.{NoShrink, TransactionGen}
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.{ScalaCheckPropertyChecks => PropertyChecks}

class OnlyTransferIsAllowedTest extends PropSpec with PropertyChecks with Matchers with TransactionGen with NoShrink {

  property("transfer is allowed but lease is not due to predicate") {

    val scriptText =
      s"""
         |
         | match tx {
         |  case ttx: TransferTransaction | MassTransferTransaction =>
         |     sigVerify(ttx.bodyBytes,ttx.proofs[0],ttx.senderPublicKey)
         |  case other =>
         |     false
         | }
      """.stripMargin
    val untyped         = Parser.parseExpr(scriptText).get.value
    val transferAllowed = ExpressionCompiler(compilerContext(V1, Expression, isAssetScript = false), untyped).explicitGet()._1

    forAll(preconditionsTransferAndLease(transferAllowed)) {
      case (genesis, script, lease, transfer) =>
        assertDiffAndState(Seq(TestBlock.create(Seq(genesis, script))), TestBlock.create(Seq(transfer)), smartEnabledFS) { case _ => () }
        assertDiffEi(Seq(TestBlock.create(Seq(genesis, script))), TestBlock.create(Seq(lease)), smartEnabledFS)(totalDiffEi =>
          totalDiffEi should produce("TransactionNotAllowedByScript"))
    }
  }

}
