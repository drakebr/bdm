package com.bdmplatform.it

import com.bdmplatform.api.http.ApiError.TransactionNotAllowedByAssetScript
import com.bdmplatform.api.http.assets.{SignedIssueV1Request, SignedIssueV2Request}
import com.bdmplatform.common.utils.{Base58, EitherExt2}
import com.bdmplatform.it.api.SyncHttpApi.AssertiveApiError
import com.bdmplatform.it.util._
import com.bdmplatform.lang.script.Script
import com.bdmplatform.lang.v2.estimator.ScriptEstimatorV2
import com.bdmplatform.state.DataEntry
import com.bdmplatform.transaction.assets.{IssueTransactionV1, IssueTransactionV2}
import com.bdmplatform.transaction.smart.script.ScriptCompiler

package object sync {
  val smartFee: Long                   = 0.004.bdm
  val minFee: Long                     = 0.001.bdm
  val leasingFee: Long                 = 0.002.bdm
  val issueFee: Long                   = 1.bdm
  val reissueFee: Long                 = 1.bdm
  val burnFee: Long                    = 1.bdm
  val sponsorFee: Long                 = 1.bdm
  val setAssetScriptFee: Long          = 1.bdm
  val setScriptFee: Long               = 0.01.bdm
  val transferAmount: Long             = 10.bdm
  val leasingAmount: Long              = transferAmount
  val issueAmount: Long                = transferAmount
  val massTransferFeePerTransfer: Long = 0.0005.bdm
  val someAssetAmount: Long            = 9999999999999L
  val matcherFee: Long                 = 0.003.bdm
  val orderFee: Long                   = matcherFee
  val smartMatcherFee: Long            = 0.007.bdm
  val smartMinFee: Long                = minFee + smartFee

  def calcDataFee(data: List[DataEntry[_]]): Long = {
    val dataSize = data.map(_.toBytes.length).sum + 128
    if (dataSize > 1024) {
      minFee * (dataSize / 1024 + 1)
    } else minFee
  }

  def calcMassTransferFee(numberOfRecipients: Int): Long = {
    minFee + massTransferFeePerTransfer * (numberOfRecipients + 1)
  }

  val supportedVersions: List[Byte] = List(1, 2)

  val script: Script       = ScriptCompiler(s"""true""".stripMargin, isAssetScript = false, ScriptEstimatorV2).explicitGet()._1
  val scriptBase64: String = script.bytes.value.base64
  val scriptBase64Raw: String = script.bytes.value.base64Raw

  val errNotAllowedByToken = "Transaction is not allowed by token-script"
  val errNotAllowedByTokenApiError: AssertiveApiError =
    AssertiveApiError(
      TransactionNotAllowedByAssetScript.Id,
      TransactionNotAllowedByAssetScript.Message,
      TransactionNotAllowedByAssetScript.Code
    )

  def createSignedIssueRequest(tx: IssueTransactionV1): SignedIssueV1Request = {
    import tx._
    SignedIssueV1Request(
      Base58.encode(tx.sender),
      new String(name),
      new String(description),
      quantity,
      decimals,
      reissuable,
      fee,
      timestamp,
      signature.base58
    )
  }

  def createSignedIssueRequest(tx: IssueTransactionV2): SignedIssueV2Request = {
    import tx._
    SignedIssueV2Request(
      Base58.encode(tx.sender),
      new String(name),
      new String(description),
      quantity,
      decimals,
      reissuable,
      fee,
      timestamp,
      proofs.proofs.map(_.toString),
      tx.script.map(_.bytes().base64)
    )
  }

}
