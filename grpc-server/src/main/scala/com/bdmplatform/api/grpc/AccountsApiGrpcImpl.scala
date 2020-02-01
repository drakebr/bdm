package com.bdmplatform.api.grpc

import com.google.protobuf.wrappers.{BytesValue, StringValue}
import com.bdmplatform.account.Alias
import com.bdmplatform.api.common.CommonAccountApi
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.protobuf.Amount
import com.bdmplatform.protobuf.transaction.PBTransactions
import com.bdmplatform.state.Blockchain
import com.bdmplatform.transaction.Asset.IssuedAsset
import io.grpc.stub.StreamObserver
import monix.execution.Scheduler
import monix.reactive.Observable

import scala.concurrent.Future
import scala.util.control.NonFatal

class AccountsApiGrpcImpl(blockchain: Blockchain)(implicit sc: Scheduler) extends AccountsApiGrpc.AccountsApi {
  private[this] val commonApi = new CommonAccountApi(blockchain)

  override def getBalances(request: BalancesRequest, responseObserver: StreamObserver[BalanceResponse]): Unit = responseObserver.interceptErrors {
    val bdmOption = if (request.assets.exists(_.isEmpty)) {
      val details = commonApi.balanceDetails(request.address.toAddress)
      Some(
        BalanceResponse.BdmBalances(details.regular, details.generating, details.available, details.effective, details.leaseIn, details.leaseOut))
    } else {
      None
    }

    val assetIdSet = request.assets.toSet
    val assets =
      if (assetIdSet.isEmpty)
        Observable.empty
      else
        Observable
          .defer(Observable.fromIterable(commonApi.portfolio(request.address.toAddress)))
          .collect {
            case (IssuedAsset(assetId), balance) if request.assets.isEmpty || assetIdSet.contains(assetId.toPBByteString) =>
              Amount(assetId, balance)
          }

    val resultStream = Observable
      .fromIterable(bdmOption)
      .map(wb => BalanceResponse().withBdm(wb))
      .++(assets.map(am => BalanceResponse().withAsset(am)))

    responseObserver.completeWith(resultStream)
  }

  override def getScript(request: AccountRequest): Future[ScriptData] = Future {
    val desc = commonApi.script(request.address.toAddress)
    ScriptData(desc.script.getOrElse(ByteStr.empty).toPBByteString, desc.scriptText.getOrElse(""), desc.complexity)
  }

  override def getActiveLeases(request: AccountRequest, responseObserver: StreamObserver[TransactionResponse]): Unit = responseObserver.interceptErrors {
    val transactions = commonApi.activeLeases(request.address.toAddress)
    val result = transactions.map { case (height, transaction) => TransactionResponse(transaction.id(), height, Some(transaction.toPB)) }
    responseObserver.completeWith(result)
  }

  override def getDataEntries(request: DataRequest, responseObserver: StreamObserver[DataEntryResponse]): Unit = responseObserver.interceptErrors {
    val stream = commonApi
      .dataStream(request.address.toAddress, key => request.key.isEmpty || key.matches(request.key))
      .map(de => DataEntryResponse(request.address, Some(PBTransactions.toPBDataEntry(de))))

    responseObserver.completeWith(stream)
  }

  override def resolveAlias(request: StringValue): Future[BytesValue] =
    Future {
      val result = for {
        alias   <- Alias.create(request.value)
        address <- blockchain.resolveAlias(alias)
      } yield BytesValue(address.bytes)

      result.explicitGetErr()
    }
}
