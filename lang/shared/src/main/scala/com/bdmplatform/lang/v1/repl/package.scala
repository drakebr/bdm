package com.bdmplatform.lang.v1

import cats.implicits._
import com.bdmplatform.lang.directives.DirectiveSet.contractDirectiveSet
import com.bdmplatform.lang.directives.values.V3
import com.bdmplatform.lang.v1.evaluator.ctx.impl.bdm.BdmContext
import com.bdmplatform.lang.v1.evaluator.ctx.impl.{CryptoContext, PureContext}
import com.bdmplatform.lang.v1.repl.node.ErrorMessageEnvironment
import com.bdmplatform.lang.v1.repl.node.http.{NodeConnectionSettings, WebEnvironment}
import com.bdmplatform.lang.v1.traits.Environment

import scala.concurrent.Future

package object repl {
  val global: BaseGlobal = com.bdmplatform.lang.Global
  val internalVarPrefixes: Set[Char] = Set('@', '$')
  val internalFuncPrefix: String = "_"

  val initialCtx: CTX[Environment] =
    CryptoContext.build(global, V3).withEnvironment[Environment] |+|
    PureContext.build(global, V3).withEnvironment[Environment]   |+|
    BdmContext.build(contractDirectiveSet)

  def buildEnvironment(settings: Option[NodeConnectionSettings]): Environment[Future] =
    settings.fold(ErrorMessageEnvironment: Environment[Future])(WebEnvironment)
}
