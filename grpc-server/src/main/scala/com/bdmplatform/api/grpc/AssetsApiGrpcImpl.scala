package com.bdmplatform.api.grpc

import com.google.protobuf.ByteString
import com.bdmplatform.api.common.CommonAssetsApi
import com.bdmplatform.api.http.ApiError.TransactionDoesNotExist
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.features.EstimatorProvider._
import com.bdmplatform.lang.script.Script
import com.bdmplatform.state.Blockchain
import com.bdmplatform.transaction.Asset.IssuedAsset
import monix.execution.Scheduler

import scala.concurrent.Future

class AssetsApiGrpcImpl(blockchain: Blockchain)(implicit sc: Scheduler) extends AssetsApiGrpc.AssetsApi {
  private[this] val commonApi = new CommonAssetsApi(blockchain)

  override def getInfo(request: AssetRequest): Future[AssetInfoResponse] = Future {
    val result = for (info <- commonApi.fullInfo(IssuedAsset(request.assetId)))
      yield
        AssetInfoResponse(
          info.description.issuer,
          ByteString.copyFrom(info.description.name),
          ByteString.copyFrom(info.description.description),
          info.description.decimals,
          info.description.reissuable,
          info.description.totalVolume.longValue(),
          info.description.script.map(
            script =>
              ScriptData(
                script.bytes().toPBByteString,
                script.expr.toString,
                Script.estimate(script, blockchain.estimator).explicitGet()
              )
          ),
          info.description.sponsorship,
          Some(info.issueTransaction.toPB),
          info.sponsorBalance.getOrElse(0)
        )
    result.explicitGetErr(TransactionDoesNotExist)
  }
}
