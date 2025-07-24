package tarot.api.endpoints

import tarot.api.dto.tarot.*
import tarot.api.dto.tarot.spreads.*
import tarot.api.infrastructure.AuthValidator
import tarot.application.commands.*
import tarot.application.commands.spreads.{CardCreateCommand, SpreadCreateCommand, SpreadPublishCommand}
import tarot.domain.models.auth.Role
import tarot.domain.models.contracts.TarotChannelType
import tarot.domain.models.spreads.SpreadId
import tarot.layers.AppEnv
import zio.ZIO
import zio.http.*
import zio.http.Method.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint

object SpreadEndpoint {
  private final val spread = "spread"
  private final val cards = "cards"
  private final val spreadTag = "spreads"

  private final val spreadCreatePath =
    Root / PathBuilder.apiPath / TarotChannelType.Telegram / spread

  private final val spreadPublishPath =
    Root / PathBuilder.apiPath / spread / uuid("spreadId") / "publish"

  private final val cardCreatePath =
    Root / PathBuilder.apiPath / TarotChannelType.Telegram / spread / uuid("spreadId") / cards / int("index")

  private val postSpreadEndpoint =
    Endpoint(POST / spreadCreatePath)
      .in[TelegramSpreadCreateRequest](MediaType.application.json)
      .out[String]
      .outErrors(
        HttpCodec.error[TarotErrorResponse](Status.BadRequest),
        HttpCodec.error[TarotErrorResponse](Status.InternalServerError)
      )
      .tag(spreadTag)

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
    Endpoint(POST / cardCreatePath)
      .in[TelegramCardCreateRequest](MediaType.application.json)
      .out[String]
      .outErrors(
        HttpCodec.error[TarotErrorResponse](Status.BadRequest),
        HttpCodec.error[TarotErrorResponse](Status.NotFound),
        HttpCodec.error[TarotErrorResponse](Status.InternalServerError)
      )
      .tag(spreadTag)

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
    Endpoint(PUT / spreadPublishPath)
      .in[SpreadPublishRequest](MediaType.application.json)
      .out[Unit]
      .outErrors(
        HttpCodec.error[TarotErrorResponse](Status.BadRequest),
        HttpCodec.error[TarotErrorResponse](Status.NotFound),
        HttpCodec.error[TarotErrorResponse](Status.Conflict),
        HttpCodec.error[TarotErrorResponse](Status.InternalServerError)
      )
      .tag(spreadTag)

  private val publishSpreadRoute = publishSpreadEndpoint.implement { case (spreadId, request) =>
    (for {
      _ <- ZIO.logInfo(s"Received request to publish spread: $spreadId")
      _ <- SpreadPublishRequest.validate(request)

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
      @@ AuthValidator.requireRole(Role.Admin)
}
