package com.bdmplatform.state

import java.util.concurrent.TimeUnit

import com.bdmplatform.account.KeyPair
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.lang.directives.values._
import com.bdmplatform.lang.script.v1.ExprScript
import com.bdmplatform.lang.utils._
import com.bdmplatform.lang.v1.compiler.ExpressionCompiler
import com.bdmplatform.lang.v1.parser.Parser
import com.bdmplatform.settings.FunctionalitySettings
import com.bdmplatform.state.StateSyntheticBenchmark._
import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.transaction.Transaction
import com.bdmplatform.transaction.smart.SetScriptTransaction
import com.bdmplatform.transaction.transfer._
import org.openjdk.jmh.annotations._
import org.scalacheck.Gen

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
@Threads(1)
@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
class StateSyntheticBenchmark {

  @Benchmark
  def appendBlock_test(db: St): Unit = db.genAndApplyNextBlock()

  @Benchmark
  def appendBlock_smart_test(db: SmartSt): Unit = db.genAndApplyNextBlock()

}

object StateSyntheticBenchmark {

  @State(Scope.Benchmark)
  class St extends BaseState {
    protected override def txGenP(sender: KeyPair, ts: Long): Gen[Transaction] =
      for {
        amount    <- Gen.choose(1, bdm(1))
        recipient <- accountGen
      } yield TransferTransactionV1.selfSigned(Bdm, sender, recipient, amount, ts, Bdm, 100000, Array.emptyByteArray).explicitGet()
  }

  @State(Scope.Benchmark)
  class SmartSt extends BaseState {

    override protected def updateFunctionalitySettings(base: FunctionalitySettings): FunctionalitySettings = {
      base.copy(preActivatedFeatures = Map(4.toShort -> 0))
    }

    protected override def txGenP(sender: KeyPair, ts: Long): Gen[Transaction] =
      for {
        recipient: KeyPair <- accountGen
        amount                    <- Gen.choose(1, bdm(1))
      } yield
        TransferTransactionV2
          .selfSigned(
            Bdm,
            sender,
            recipient.toAddress,
            amount,
            ts,
            Bdm,
            1000000,
            Array.emptyByteArray
          )
          .explicitGet()

    @Setup
    override def init(): Unit = {
      super.init()

      val textScript    = "sigVerify(tx.bodyBytes,tx.proofs[0],tx.senderPublicKey)"
      val untypedScript = Parser.parseExpr(textScript).get.value
      val typedScript   = ExpressionCompiler(compilerContext(V1, Expression, isAssetScript = false), untypedScript).explicitGet()._1

      val setScriptBlock = nextBlock(
        Seq(
          SetScriptTransaction
            .selfSigned(
              richAccount,
              Some(ExprScript(typedScript).explicitGet()),
              1000000,
              System.currentTimeMillis()
            )
            .explicitGet()
        )
      )

      applyBlock(setScriptBlock)
    }
  }

}
