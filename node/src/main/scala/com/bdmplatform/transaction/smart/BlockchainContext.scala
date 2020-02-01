package com.bdmplatform.transaction.smart

import cats.Id
import cats.kernel.Monoid
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.lang.directives.DirectiveSet
import com.bdmplatform.lang.directives.values.{ContentType, ScriptType, StdLibVersion}
import com.bdmplatform.lang.v1.evaluator.ctx.EvaluationContext
import com.bdmplatform.lang.v1.evaluator.ctx.impl.bdm.BdmContext
import com.bdmplatform.lang.v1.evaluator.ctx.impl.{CryptoContext, PureContext}
import com.bdmplatform.lang.v1.traits.Environment
import com.bdmplatform.lang.{ExecutionError, Global}
import com.bdmplatform.state._
import monix.eval.Coeval

object BlockchainContext {

  type In = BdmEnvironment.In
  def build(version: StdLibVersion,
            nByte: Byte,
            in: Coeval[In],
            h: Coeval[Int],
            blockchain: Blockchain,
            isTokenContext: Boolean,
            isContract: Boolean,
            address: Coeval[ByteStr]): Either[ExecutionError, EvaluationContext[Environment, Id]] =
    DirectiveSet(
      version,
      ScriptType.isAssetScript(isTokenContext),
      ContentType.isDApp(isContract)
    ).map(BdmContext.build)
      .map(
        Seq(
          PureContext.build(Global, version).withEnvironment[Environment],
          CryptoContext.build(Global, version).withEnvironment[Environment],
          _
        )
      )
      .map(Monoid.combineAll(_))
      .map(_.evaluationContext(new BdmEnvironment(nByte, in, h, blockchain, address)))
}
