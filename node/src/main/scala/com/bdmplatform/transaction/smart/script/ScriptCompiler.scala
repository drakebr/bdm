package com.bdmplatform.transaction.smart.script

import com.bdmplatform.lang.directives.Directive.extractValue
import com.bdmplatform.lang.directives.DirectiveKey._
import com.bdmplatform.lang.directives._
import com.bdmplatform.lang.directives.values._
import com.bdmplatform.lang.script.v1.ExprScript
import com.bdmplatform.lang.script.{ContractScript, Script, ScriptPreprocessor}
import com.bdmplatform.lang.utils._
import com.bdmplatform.lang.v1.compiler.{ContractCompiler, ExpressionCompiler}
import com.bdmplatform.lang.v1.estimator.{ScriptEstimator, ScriptEstimatorV1}
import com.bdmplatform.utils._

object ScriptCompiler extends ScorexLogging {

  @Deprecated
  def apply(
    scriptText:    String,
    isAssetScript: Boolean,
    estimator:     ScriptEstimator
  ): Either[String, (Script, Long)] =
    for {
      directives <- DirectiveParser(scriptText)
      contentType = extractValue(directives, CONTENT_TYPE)
      version     = extractValue(directives, STDLIB_VERSION)
      scriptType  = if (isAssetScript) Asset else Account
      _      <- DirectiveSet(version, scriptType, contentType)
      script <- tryCompile(scriptText, contentType, version, isAssetScript)
      complexity <- Script.estimate(script, estimator)
    } yield (script, complexity)

  def compile(
    scriptText: String,
    estimator:  ScriptEstimator,
    libraries:  Map[String, String] = Map()
  ): Either[String, (Script, Long)] = {
    for {
      directives  <- DirectiveParser(scriptText)
      ds          <- Directive.extractDirectives(directives)
      linkedInput <- ScriptPreprocessor(scriptText, libraries, ds.imports)
      result      <- apply(linkedInput, ds.scriptType == Asset, estimator)
    } yield result
  }

  private def tryCompile(src: String, cType: ContentType, version: StdLibVersion, isAssetScript: Boolean): Either[String, Script] = {
    val ctx = compilerContext(version, cType, isAssetScript)
    try {
      cType match {
        case Expression => ExpressionCompiler.compile(src, ctx).flatMap(expr => ExprScript.apply(version, expr))
        case DApp       => ContractCompiler.compile(src, ctx).flatMap(expr => ContractScript.apply(version, expr))
        case Library    => ExpressionCompiler.compileDecls(src, ctx).flatMap(ExprScript(version, _))
      }
    } catch {
      case ex: Throwable =>
        log.error("Error compiling script", ex)
        log.error(src)
        val msg = Option(ex.getMessage).getOrElse("Parsing failed: Unknown error")
        Left(msg)
    }
  }

  def estimate(script: Script, version: StdLibVersion): Either[String, Long] =
    Script.estimate(script, ScriptEstimatorV1)
}
