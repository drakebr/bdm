package com.bdmplatform.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.bdmplatform.account.Address
import com.bdmplatform.api.http.assets.{AssetsApiRoute, TransferV1Request, TransferV2Request}
import com.bdmplatform.http.ApiMarshallers._
import com.bdmplatform.state.Blockchain
import com.bdmplatform.transaction.transfer._
import com.bdmplatform.wallet.Wallet
import com.bdmplatform.{RequestGen, TestTime}
import org.scalamock.scalatest.PathMockFactory
import org.scalatest.concurrent.Eventually
import play.api.libs.json.Writes

class AssetsRouteSpec extends RouteSpec("/assets") with RequestGen with PathMockFactory with Eventually with RestAPISettingsHelper {

  private val wallet = stub[Wallet]
  private val state = stub[Blockchain]

  private val seed               = "seed".getBytes("UTF-8")
  private val senderPrivateKey   = Wallet.generateNewAccount(seed, 0)
  private val receiverPrivateKey = Wallet.generateNewAccount(seed, 1)

  (wallet.privateKeyAccount _).when(senderPrivateKey.toAddress).onCall((_: Address) => Right(senderPrivateKey)).anyNumberOfTimes()

  "/transfer" - {
    val route: Route = AssetsApiRoute(restAPISettings, wallet, DummyUtxPoolSynchronizer.accepting, state, new TestTime()).route

    def posting[A: Writes](v: A): RouteTestResult = Post(routePath("/transfer"), v).addHeader(ApiKeyHeader) ~> route

    "accepts TransferRequest" in {
      val req = TransferV1Request(
        assetId = None,
        feeAssetId = None,
        amount = 1 * Bdm,
        fee = Bdm / 3,
        sender = senderPrivateKey.stringRepr,
        attachment = Some("attachment"),
        recipient = receiverPrivateKey.stringRepr,
        timestamp = Some(System.currentTimeMillis())
      )

      posting(req) ~> check {
        status shouldBe StatusCodes.OK
        responseAs[TransferTransactionV1]
      }
    }

    "accepts VersionedTransferRequest" in {
      val req = TransferV2Request(
        assetId = None,
        amount = 1 * Bdm,
        feeAssetId = None,
        fee = Bdm / 3,
        sender = senderPrivateKey.stringRepr,
        attachment = None,
        recipient = receiverPrivateKey.stringRepr,
        timestamp = Some(System.currentTimeMillis())
      )

      posting(req) ~> check {
        status shouldBe StatusCodes.OK
        responseAs[TransferV2Request]
      }
    }

    "returns a error if it is not a transfer request" in {
      val req = issueReq.sample.get
      posting(req) ~> check {
        status shouldNot be(StatusCodes.OK)
      }
    }
  }

}
