package com.bdmplatform.lang

import cats.kernel.Monoid
import com.bdmplatform.lang.directives.values.V2
import com.bdmplatform.lang.v1.compiler.ExpressionCompiler
import com.bdmplatform.lang.v1.compiler.Terms.EXPR
import com.bdmplatform.lang.v1.evaluator.ctx.impl.bdm.BdmContext
import com.bdmplatform.lang.v1.evaluator.ctx.impl.{CryptoContext, PureContext}

object JavaAdapter {
  private val version = V2

  lazy val ctx =
    Monoid.combineAll(
      Seq(
        CryptoContext.compilerContext(Global, version),
        BdmContext.build(???).compilerContext,
        PureContext.build(Global, version).compilerContext
      ))

  def compile(input: String): EXPR = {
    ExpressionCompiler
      .compile(input, ctx)
      .fold(
        error => throw new IllegalArgumentException(error),
        expr => expr
      )
  }
}
