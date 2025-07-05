package tarot.api.endpoints

import tarot.api.dto.*
import tarot.api.endpoints.SpreadRoutes.*
import tarot.application.commands.*
import tarot.domain.models.contracts.{SpreadId, TarotChannelType}
import tarot.layers.AppEnv
import zio.ZIO
import zio.http.*
import zio.http.Method.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint

object SpreadEndpoint {
  private val postSpreadEndpoint =
    Endpoint(POST / spreadPath)
      .in[TelegramSpreadRequest](MediaType.application.json)
      .out[String]
      .outErrors(
        HttpCodec.error[TarotErrorResponse](Status.BadRequest),
        HttpCodec.error[TarotErrorResponse](Status.InternalServerError)
      )
      .tag(spreadTag)

  private val postSpreadRoute = postSpreadEndpoint.implement { request =>
    (for {
      _ <- ZIO.logInfo(s"Received request to create spread: ${request.title}")    
      externalSpread <- TelegramSpreadRequest.fromTelegram(request)
      
      spreadCreateCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.spreadCreateCommandHandler)
      spreadCreateCommand = SpreadCreateCommand(externalSpread)
      spreadId <- spreadCreateCommandHandler.handle(spreadCreateCommand)
    } yield spreadId)
      .mapBoth(
        error => TarotErrorMapper.toResponse(error),
        spreadId => spreadId.id.toString)
  }

  private val postCardEndpoint =
    Endpoint(POST / cardPath)
      .in[TelegramCardRequest](MediaType.application.json)
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
      externalCard <- TelegramCardRequest.fromTelegram(request, index, spreadId)

      cardCreateCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.cardCreateCommandHandler)
      cardCreateCommand = CardCreateCommand(externalCard)
      cardId <- cardCreateCommandHandler.handle(cardCreateCommand)
    } yield cardId)
      .mapBoth(
        error => TarotErrorMapper.toResponse(error),
        cardId => cardId.id.toString)
  }

  private val publishSpreadEndpoint =
    Endpoint(PUT / publishSpreadPath)
      .out[Unit]
      .outErrors(
        HttpCodec.error[TarotErrorResponse](Status.BadRequest),
        HttpCodec.error[TarotErrorResponse](Status.NotFound),
        HttpCodec.error[TarotErrorResponse](Status.Conflict),
        HttpCodec.error[TarotErrorResponse](Status.InternalServerError)
      )
      .tag(spreadTag)

  private val publishSpreadRoute = publishSpreadEndpoint.implement { spreadId =>
    (for {
      _ <- ZIO.logInfo(s"Received request to publish spread: $spreadId")

      spreadPublishCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.spreadPublishCommandHandler)
      spreadPublishCommand = SpreadPublishCommand(SpreadId(spreadId))
      _ <- spreadPublishCommandHandler.handle(spreadPublishCommand)
    } yield ())
      .mapError(error => TarotErrorMapper.toResponse(error))
  }

  val allEndpoints: List[Endpoint[?, ?, ?, ?, ?]] =
    List(postSpreadEndpoint, postCardEndpoint, publishSpreadEndpoint)

  val allRoutes: Routes[AppEnv, Response] =
    Routes(postSpreadRoute, postCardRoute, publishSpreadRoute)
}
