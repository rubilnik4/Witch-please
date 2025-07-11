package tarot.api.endpoints

import tarot.api.dto.tarot.*
import tarot.api.dto.tarot.telegram.*
import tarot.api.middlewares.AuthMiddleware
import tarot.application.commands.*
import tarot.domain.models.auth.Role
import tarot.domain.models.spreads.SpreadId
import tarot.layers.AppEnv
import zio.ZIO
import zio.http.*
import zio.http.Method.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint

object SpreadEndpoint {
  private val postSpreadEndpoint =
    Endpoint(POST / SpreadPaths.spreadCreatePath)
      .in[TelegramSpreadCreateRequest](MediaType.application.json)
      .out[String]
      .outErrors(
        HttpCodec.error[TarotErrorResponse](Status.BadRequest),
        HttpCodec.error[TarotErrorResponse](Status.InternalServerError)
      )
      .tag(SpreadPaths.spreadTag)

  private val postSpreadRoute = postSpreadEndpoint.implement { request =>
    (for {
      _ <- ZIO.logInfo(s"Received request to create spread: ${request.title}")
      externalSpread <- TelegramSpreadCreateRequest.fromTelegram(request)

      spreadCreateCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.spreadCreateCommandHandler)
      spreadCreateCommand = SpreadCreateCommand(externalSpread)
      spreadId <- spreadCreateCommandHandler.handle(spreadCreateCommand)
    } yield spreadId)
      .mapBoth(
        error => TarotErrorResponse.toResponse(error),
        spreadId => spreadId.id.toString)
  }

  private val postCardEndpoint =
    Endpoint(POST / SpreadPaths.cardCreatePath)
      .in[TelegramCardCreateRequest](MediaType.application.json)
      .out[String]
      .outErrors(
        HttpCodec.error[TarotErrorResponse](Status.BadRequest),
        HttpCodec.error[TarotErrorResponse](Status.NotFound),
        HttpCodec.error[TarotErrorResponse](Status.InternalServerError)
      )
      .tag(SpreadPaths.spreadTag)

  private val postCardRoute = postCardEndpoint.implement { case (spreadId, index, request) =>
    (for {
      _ <- ZIO.logInfo(s"Received request to create card number $index for spread $spreadId")
      externalCard <- TelegramCardCreateRequest.fromTelegram(request, index, spreadId)

      cardCreateCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.cardCreateCommandHandler)
      cardCreateCommand = CardCreateCommand(externalCard)
      cardId <- cardCreateCommandHandler.handle(cardCreateCommand)
    } yield cardId)
      .mapBoth(
        error => TarotErrorResponse.toResponse(error),
        cardId => cardId.id.toString)
  }

  private val publishSpreadEndpoint =
    Endpoint(PUT / SpreadPaths.spreadPublishPath)
      .in[SpreadPublishRequest](MediaType.application.json)
      .out[Unit]
      .outErrors(
        HttpCodec.error[TarotErrorResponse](Status.BadRequest),
        HttpCodec.error[TarotErrorResponse](Status.NotFound),
        HttpCodec.error[TarotErrorResponse](Status.Conflict),
        HttpCodec.error[TarotErrorResponse](Status.InternalServerError)
      )
      .tag(SpreadPaths.spreadTag)

  private val publishSpreadRoute = publishSpreadEndpoint.implement { case (spreadId, request) =>
    (for {
      _ <- ZIO.logInfo(s"Received request to publish spread: $spreadId")
      externalCard <- SpreadPublishRequest.validate(request)

      spreadPublishCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.spreadPublishCommandHandler)
      spreadPublishCommand = SpreadPublishCommand(SpreadId(spreadId), request.scheduledAt)
      _ <- spreadPublishCommandHandler.handle(spreadPublishCommand)
    } yield ())
      .mapError(error => TarotErrorResponse.toResponse(error))
  }

  val allEndpoints: List[Endpoint[?, ?, ?, ?, ?]] =
    List(postSpreadEndpoint, postCardEndpoint, publishSpreadEndpoint)

  val allRoutes: Routes[AppEnv, Response] =
    Routes(postSpreadRoute, postCardRoute, publishSpreadRoute)
      @@ AuthMiddleware.requireRole(Role.Admin)
}
