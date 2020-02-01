package com.bdmplatform.lang.v1.evaluator.ctx.impl.bdm

import cats.implicits._
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.lang.directives.values._
import com.bdmplatform.lang.directives.{DirectiveDictionary, DirectiveSet}
import com.bdmplatform.lang.v1.CTX
import com.bdmplatform.lang.v1.evaluator.ctx.impl.bdm.Functions._
import com.bdmplatform.lang.v1.evaluator.ctx.impl.bdm.Types._
import com.bdmplatform.lang.v1.evaluator.ctx.impl.bdm.Vals._
import com.bdmplatform.lang.v1.traits._

object BdmContext {
  def build(ds: DirectiveSet): CTX[Environment] =
    invariableCtx |+| variableCtxCache(ds)

  private val commonFunctions =
    Array(
      txHeightByIdF,
      getIntegerFromStateF,
      getBooleanFromStateF,
      getBinaryFromStateF,
      getStringFromStateF,
      getIntegerFromArrayF,
      getBooleanFromArrayF,
      getBinaryFromArrayF,
      getStringFromArrayF,
      getIntegerByIndexF,
      getBooleanByIndexF,
      getBinaryByIndexF,
      getStringByIndexF,
      addressFromPublicKeyF,
      addressFromStringF,
      addressFromRecipientF,
      assetBalanceF,
      bdmBalanceF
    )

  private val invariableCtx =
    CTX(Seq(), Map(height), commonFunctions)

  private val allDirectives =
    for {
      version     <- DirectiveDictionary[StdLibVersion].all
      scriptType  <- DirectiveDictionary[ScriptType].all
      contentType <- DirectiveDictionary[ContentType].all
    } yield DirectiveSet(version, scriptType, contentType)

  private val variableCtxCache: Map[DirectiveSet, CTX[Environment]] =
    allDirectives
      .filter(_.isRight)
      .map(_.explicitGet())
      .map(ds => (ds, variableCtx(ds)))
      .toMap

  private def variableCtx(ds: DirectiveSet): CTX[Environment] = {
    val isTokenContext = ds.scriptType match {
      case Account => false
      case Asset   => true
    }
    val proofsEnabled = !isTokenContext
    val version = ds.stdLibVersion
    CTX(
      variableTypes(version, proofsEnabled),
      variableVars(isTokenContext, version, ds.contentType, proofsEnabled),
      variableFuncs(version, proofsEnabled)
    )
  }

  private def variableFuncs(version: StdLibVersion, proofsEnabled: Boolean) =
    version match {
      case V1 | V2 => Array(txByIdF(proofsEnabled, version))
      case V3 =>
        extractedFuncs ++ Array(assetInfoF, blockInfoByHeightF, transferTxByIdF(proofsEnabled, version), stringFromAddressF)
    }

  private def variableVars(
    isTokenContext: Boolean,
    version:        StdLibVersion,
    contentType:    ContentType,
    proofsEnabled:  Boolean
  ) = {
    val txVal = tx(isTokenContext, version, proofsEnabled)
    version match {
      case V1 => Map(txVal)
      case V2 => Map(sell, buy, txVal)
      case V3 =>
        val `this` = if (isTokenContext) assetThis else accountThis
        val txO = if (contentType == Expression) Map(txVal) else Map()
        val common = Map(sell, buy, lastBlock, `this`)
        common ++ txO
    }
  }

  private def variableTypes(version: StdLibVersion, proofsEnabled: Boolean) =
    buildBdmTypes(proofsEnabled, version) ++ (if (version == V3) dAppTypes else Nil)
}
