package com.bdmplatform.api.http.leasing

import akka.http.scaladsl.server.Route
import com.bdmplatform.account.Address
import com.bdmplatform.api.common.CommonAccountApi
import com.bdmplatform.api.http._
import com.bdmplatform.http.BroadcastRoute
import com.bdmplatform.network.UtxPoolSynchronizer
import com.bdmplatform.settings.RestAPISettings
import com.bdmplatform.state.Blockchain
import com.bdmplatform.transaction._
import com.bdmplatform.transaction.lease.LeaseTransaction
import com.bdmplatform.utils.Time
import com.bdmplatform.wallet.Wallet
import io.swagger.annotations._
import javax.ws.rs.Path
import play.api.libs.json.JsNumber

@Path("/leasing")
@Api(value = "/leasing")
case class LeaseApiRoute(settings: RestAPISettings, wallet: Wallet, blockchain: Blockchain, utxPoolSynchronizer: UtxPoolSynchronizer, time: Time)
    extends ApiRoute
    with BroadcastRoute
    with AuthRoute {

  private[this] val commonAccountApi = new CommonAccountApi(blockchain)

  override val route = pathPrefix("leasing") {
    active ~ deprecatedRoute
  }

  private def deprecatedRoute: Route =
    (path("lease") & withAuth) {
      broadcast[LeaseV1Request](TransactionFactory.leaseV1(_, wallet, time))
    } ~ (path("cancel") & withAuth) {
      broadcast[LeaseCancelV1Request](TransactionFactory.leaseCancelV1(_, wallet, time))
    } ~ pathPrefix("broadcast") {
      path("lease")(broadcast[SignedLeaseV1Request](_.toTx)) ~
        path("cancel")(broadcast[SignedLeaseCancelV1Request](_.toTx))
    }

  @Path("/active/{address}")
  @ApiOperation(value = "Get all active leases for an address", httpMethod = "GET")
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(name = "address", value = "Wallet address ", required = true, dataType = "string", paramType = "path")
    )
  )
  def active: Route = (pathPrefix("active") & get & extractScheduler) { implicit sc =>
    pathPrefix(Segment) { address =>
      complete(Address.fromString(address) match {
        case Left(e) => ApiError.fromValidationError(e)
        case Right(a) =>
          commonAccountApi
            .activeLeases(a)
            .collect {
              case (height, leaseTransaction: LeaseTransaction) =>
                leaseTransaction.json() + ("height" -> JsNumber(height))
            }
            .toListL
            .runToFuture
      })
    }
  }
}
