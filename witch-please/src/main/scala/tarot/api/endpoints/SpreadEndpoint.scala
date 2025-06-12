package tarot.api.endpoints

object SpreadEndpoint {
  private final val path = "spread"
  private final val tag = "spread"

  private val getSpreadEndpoint =
    Endpoint(GET / path / string("assetIdA") / string("assetIdB"))
      .out[SpreadResponse]
      .outErrors(
        HttpCodec.error[MarketErrorResponse](Status.BadRequest),
        HttpCodec.error[MarketErrorResponse](Status.NotFound),
        HttpCodec.error[MarketErrorResponse](Status.InternalServerError)
      )
      .tag(tag)

  private val getSpreadRoute = getSpreadEndpoint.implement { (assetIdA, assetIdB) =>
    val assetSpreadId = AssetSpreadId(AssetId(assetIdA), AssetId(assetIdB))
    val spreadQuery = SpreadQuery(assetSpreadId)
    for {
      _ <- ZIO.logInfo(s"Received request for assert spread: $assetSpreadId")

      spreadQueryHandler <- ZIO.serviceWith[AppEnv](_.marketQueryHandler.spreadQueryHandler)
      result <- spreadQueryHandler.handle(spreadQuery)
        .mapBoth(
          error => MarketErrorMapper.toResponse(error),
          spread => SpreadMapper.toResponse(spread))
    } yield result
  }

  private val computeSpreadEndpoint = {
    Endpoint(POST / path)
      .in[ComputeSpreadRequest](MediaType.application.json)
      .out[SpreadResponse]
      .outErrors(
        HttpCodec.error[MarketErrorResponse](Status.BadRequest),
        HttpCodec.error[MarketErrorResponse](Status.NotFound),
        HttpCodec.error[MarketErrorResponse](Status.InternalServerError)
      )
      .tag(tag)
  }

  private val computeSpreadRoute = computeSpreadEndpoint.implement { request =>
    val assetSpreadId = AssetSpreadId(AssetId(request.assetIdA), AssetId(request.assetIdB))
    val spreadCommand = SpreadCommand(SpreadState.Init(), assetSpreadId)
    for {
      _ <- ZIO.logInfo(s"Received request for computing spread for assets: $assetSpreadId")

      spreadCommandHandler <- ZIO.serviceWith[AppEnv](_.marketCommandHandler.spreadCommandHandler)
      result <- spreadCommandHandler.handle(spreadCommand)
        .mapBoth(
          error => MarketErrorMapper.toResponse(error),
          spreadResult => SpreadMapper.toResponse(spreadResult.spread))
    } yield result
  }

  val allEndpoints: List[Endpoint[_, _, _, _, _]] =
    List(getSpreadEndpoint, computeSpreadEndpoint)

  val allRoutes: Routes[AppEnv, Response] =
    Routes(getSpreadRoute, computeSpreadRoute)
}
