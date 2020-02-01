package com.bdmplatform.features.api

import akka.http.scaladsl.server.Route
import com.bdmplatform.api.http.ApiRoute
import com.bdmplatform.features.FeatureProvider._
import com.bdmplatform.features.{BlockchainFeatureStatus, BlockchainFeatures}
import com.bdmplatform.settings.{FeaturesSettings, RestAPISettings}
import com.bdmplatform.state.Blockchain
import io.swagger.annotations._
import javax.ws.rs.Path
import play.api.libs.json.Json

@Path("/activation")
@Api(value = "activation")
case class ActivationApiRoute(settings: RestAPISettings, featuresSettings: FeaturesSettings, blockchain: Blockchain) extends ApiRoute {

  override lazy val route: Route = pathPrefix("activation") {
    status
  }

  @Path("/status")
  @ApiOperation(value = "Status", notes = "Get activation status", httpMethod = "GET")
  @ApiResponses(
    Array(
      new ApiResponse(code = 200, message = "Json activation status")
    )
  )
  def status: Route = (get & path("status")) {
    val height = blockchain.height

    complete(
      Json.toJson(
        ActivationStatus(
          height,
          blockchain.settings.functionalitySettings.activationWindowSize(height),
          blockchain.settings.functionalitySettings.blocksForFeatureActivation(height),
          blockchain.settings.functionalitySettings.activationWindow(height).last,
          (blockchain.featureVotes(height).keySet ++
            blockchain.approvedFeatures.keySet ++
            BlockchainFeatures.implemented).toSeq.sorted.map(id => {
            val status = blockchain.featureStatus(id, height)
            FeatureActivationStatus(
              id,
              BlockchainFeatures.feature(id).fold("Unknown feature")(_.description),
              status,
              (BlockchainFeatures.implemented.contains(id), featuresSettings.supported.contains(id)) match {
                case (false, _) => NodeFeatureStatus.NotImplemented
                case (_, true)  => NodeFeatureStatus.Voted
                case _          => NodeFeatureStatus.Implemented
              },
              blockchain.featureActivationHeight(id),
              if (status == BlockchainFeatureStatus.Undefined) blockchain.featureVotes(height).get(id).orElse(Some(0)) else None
            )
          })
        )
      )
    )
  }
}
