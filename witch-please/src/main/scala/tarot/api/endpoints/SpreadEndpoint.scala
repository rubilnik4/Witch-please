package tarot.api.endpoints

import tarot.api.dto.*
import tarot.api.endpoints.SpreadRoutes.*
import tarot.application.commands.*
import tarot.domain.models.contracts.TarotChannelType
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
      
      spreadCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.spreadCommandHandler)
      spreadCommand = SpreadCreateCommand(externalSpread)
      spreadId <- spreadCommandHandler.handle(spreadCommand)
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

      cardCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.cardCommandHandler)
      cardCommand = CardCreateCommand(externalCard)
      cardId <- cardCommandHandler.handle(cardCommand)
    } yield cardId)
      .mapBoth(
        error => TarotErrorMapper.toResponse(error),
        cardId => cardId.id.toString)
  }

  private val publishSpreadEndpoint =
    Endpoint(POST / publishSpreadPath)
      .outErrors(
        HttpCodec.error[TarotErrorResponse](Status.BadRequest),
        HttpCodec.error[TarotErrorResponse](Status.NotFound),
        HttpCodec.error[TarotErrorResponse](Status.InternalServerError)
      )
      .tag(spreadTag)

  private val publishSpreadRoute = publishSpreadEndpoint.implement { spreadId =>
    (for {
      _ <- ZIO.logInfo(s"Received request to publish spread: $spreadId")

      spreadCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.spreadCommandHandler)
      spreadCommand = SpreadCreateCommand(externalSpread)
      spreadId <- spreadCommandHandler.handle(spreadCommand)
    } yield spreadId)
      .mapBoth(
        error => TarotErrorMapper.toResponse(error),
        spreadId => spreadId.id.toString)
  }

  val allEndpoints: List[Endpoint[?, ?, ?, ?, ?]] =
    List(postSpreadEndpoint, postCardEndpoint)

  val allRoutes: Routes[AppEnv, Response] =
    Routes(postSpreadRoute, postCardRoute)
}
