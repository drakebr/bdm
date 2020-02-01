package com.bdmplatform.transaction.smart.script

import cats.Id
import cats.implicits._
import com.bdmplatform.account.AddressScheme
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.lang._
import com.bdmplatform.lang.contract.DApp
import com.bdmplatform.lang.script.v1.ExprScript
import com.bdmplatform.lang.script.{ContractScript, Script}
import com.bdmplatform.lang.v1.compiler.Terms.{EVALUATED, TRUE}
import com.bdmplatform.lang.v1.evaluator.{EvaluatorV1, _}
import com.bdmplatform.state._
import com.bdmplatform.transaction.TxValidationError.GenericError
import com.bdmplatform.transaction.smart.{BlockchainContext, RealTransactionWrapper, Verifier}
import com.bdmplatform.transaction.{Authorized, Proven}
import monix.eval.Coeval

object ScriptRunner {
  type TxOrd = BlockchainContext.In

  def apply(height: Int,
            in: TxOrd,
            blockchain: Blockchain,
            script: Script,
            isAssetScript: Boolean,
            scriptContainerAddress: ByteStr): (Log[Id], Either[ExecutionError, EVALUATED]) = {
    script match {
      case s: ExprScript =>
        val ctx = BlockchainContext.build(
          script.stdLibVersion,
          AddressScheme.current.chainId,
          Coeval.evalOnce(in),
          Coeval.evalOnce(height),
          blockchain,
          isAssetScript,
          false,
          Coeval(scriptContainerAddress)
        )
        EvaluatorV1().applyWithLogging[EVALUATED](ctx, s.expr)
      case ContractScript.ContractScriptImpl(_, DApp(_, decls, _, Some(vf))) =>
        val ctx = BlockchainContext.build(
          script.stdLibVersion,
          AddressScheme.current.chainId,
          Coeval.evalOnce(in),
          Coeval.evalOnce(height),
          blockchain,
          isAssetScript,
          true,
          Coeval(scriptContainerAddress)
        )
        val evalContract = in.eliminate(
          t => ContractEvaluator.verify(decls, vf, RealTransactionWrapper.apply(t)),
          _.eliminate(t => ContractEvaluator.verify(decls, vf, RealTransactionWrapper.ord(t)), _ => ???)
        )
        EvaluatorV1().evalWithLogging(ctx, evalContract)

      case ContractScript.ContractScriptImpl(_, DApp(_, _, _, None)) =>
        val t: Proven with Authorized =
          in.eliminate(_.asInstanceOf[Proven with Authorized], _.eliminate(_.asInstanceOf[Proven with Authorized], _ => ???))
        (List.empty, Verifier.verifyAsEllipticCurveSignature[Proven with Authorized](t) match {
          case Right(_)                => Right(TRUE)
          case Left(GenericError(err)) => Left(err)
        })
      case _ => (List.empty, "Unsupported script version".asLeft[EVALUATED])
    }
  }
}
