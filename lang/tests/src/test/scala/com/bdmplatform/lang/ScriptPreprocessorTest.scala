package com.bdmplatform.lang

import cats.Id
import cats.kernel.Monoid
import com.bdmplatform.lang.Common.NoShrink
import com.bdmplatform.lang.directives.values.V3
import com.bdmplatform.lang.directives.{Directive, DirectiveParser}
import com.bdmplatform.lang.script.ScriptPreprocessor
import com.bdmplatform.lang.v1.CTX
import com.bdmplatform.lang.v1.compiler.ExpressionCompiler
import com.bdmplatform.lang.v1.compiler.Terms.{CONST_BOOLEAN, EVALUATED}
import com.bdmplatform.lang.v1.evaluator.Contextful.NoContext
import com.bdmplatform.lang.v1.evaluator.EvaluatorV1
import com.bdmplatform.lang.v1.evaluator.EvaluatorV1._
import com.bdmplatform.lang.v1.evaluator.ctx.impl.PureContext
import com.bdmplatform.lang.v1.parser.Parser
import com.bdmplatform.lang.v1.testing.ScriptGenParser
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.{ScalaCheckPropertyChecks => PropertyChecks}

class ScriptPreprocessorTest extends PropSpec with PropertyChecks with Matchers with ScriptGenParser with NoShrink {
  private val evaluator = new EvaluatorV1[Id, NoContext]()

  private def processAndEval(src: String, libraries: Map[String, String]): Either[ExecutionError, EVALUATED] =
    for {
      directives <- DirectiveParser(src)
      ds         <- Directive.extractDirectives(directives)
      linked     <- ScriptPreprocessor(src, libraries, ds.imports)
      r          <- eval(linked)
    } yield r

  private def eval(code: String): Either[String, EVALUATED] = {
    val untyped  = Parser.parseExpr(code).get.value
    val ctx: CTX[NoContext] = Monoid.combineAll(Seq(PureContext.build(Global, V3)))
    val typed    = ExpressionCompiler(ctx.compilerContext, untyped)
    typed.flatMap(v => evaluator.apply[EVALUATED](ctx.evaluationContext, v._1))
  }

  property("multiple libraries") {
    val script =
      """
        | {-# SCRIPT_TYPE ACCOUNT #-}
        | {-# IMPORT lib1,lib2,lib3 #-}
        | let a = 5
        | multiply(inc(a), dec(a)) == (5 + 1) * (5 - 1)
      """.stripMargin

    val libraries =
      Map(
        "lib1" ->
          """
            | {-# SCRIPT_TYPE  ACCOUNT #-}
            | {-# CONTENT_TYPE LIBRARY #-}
            | func inc(a: Int) = a + 1
          """.stripMargin,
        "lib2" ->
          """
            | {-# SCRIPT_TYPE  ACCOUNT #-}
            | {-# CONTENT_TYPE LIBRARY #-}
            | func dec(a: Int) = a - 1
          """.stripMargin,
        "lib3" ->
          """
            | {-# SCRIPT_TYPE  ACCOUNT #-}
            | {-# CONTENT_TYPE LIBRARY #-}
            | func multiply(a: Int, b: Int) = a * b
          """.stripMargin
      )

    processAndEval(script, libraries) shouldBe Right(CONST_BOOLEAN(true))
  }

  property("library without CONTENT_TYPE LIBRARY") {
    val script =
      """
        | {-# SCRIPT_TYPE ACCOUNT #-}
        | {-# IMPORT lib1,lib2,lib3 #-}
        | let a = 5
        | multiply(inc(a), dec(a)) == (5 + 1) * (5 - 1)
      """.stripMargin

    val libraries =
      Map(
        "lib1" ->
          """
            | {-# SCRIPT_TYPE  ACCOUNT #-}
            | {-# CONTENT_TYPE LIBRARY #-}
            | func inc(a: Int) = a + 1
          """.stripMargin,
        "lib2" ->
          """
            | {-# SCRIPT_TYPE  ACCOUNT #-}
            | {-# CONTENT_TYPE EXPRESSION #-}
            | func dec(a: Int) = a - 1
          """.stripMargin,
        "lib3" ->
          """
            | {-# SCRIPT_TYPE  ACCOUNT #-}
            | {-# CONTENT_TYPE LIBRARY #-}
            | func multiply(a: Int, b: Int) = a * b
          """.stripMargin
      )

    processAndEval(script, libraries) shouldBe Left("CONTENT_TYPE of `lib2` is not LIBRARY")
  }

  property("unresolved libraries") {
    val script =
      """
        | {-# SCRIPT_TYPE ACCOUNT #-}
        | {-# IMPORT lib1,lib2,lib3,lib4 #-}
        | let a = 5
        | multiply(inc(a), dec(a)) == (5 + 1) * (5 - 1)
      """.stripMargin

    val libraries =
      Map(
        "lib1" ->
          """
            | {-# SCRIPT_TYPE  ACCOUNT #-}
            | {-# CONTENT_TYPE LIBRARY #-}
            | func inc(a: Int) = a + 1
          """.stripMargin,
        "lib2" ->
          """
            | {-# SCRIPT_TYPE  ASSET   #-}
            | {-# CONTENT_TYPE LIBRARY #-}
            | func dec(a: Int) = a - 1
          """.stripMargin
      )

    processAndEval(script, libraries) shouldBe Left("Unresolved imports: `lib3`, `lib4`")
  }
}
