package tarot.api.endpoints

import tarot.api.dto.{TarotErrorMapper, TarotErrorResponse, TelegramCardRequest, TelegramSpreadRequest}
import tarot.application.commands.{CardCommand, SpreadCommand}
import tarot.domain.models.contracts.TarotChannelType
import tarot.layers.AppEnv
import zio.ZIO
import zio.http.*
import zio.http.Method.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint

object SpreadEndpoint {
  private final val spreadPath = "spread"
  private final val cardsPath = "cards"
  private final val tag = "spread"
  final val spreadRoute = Root / PathBuilder.apiPath / TarotChannelType.Telegram / spreadPath
  final val cardRoute = 
    Root / PathBuilder.apiPath / TarotChannelType.Telegram / spreadPath / uuid("spreadId") / cardsPath / int("index")

  private val postSpreadEndpoint =
    Endpoint(POST / spreadRoute)
      .in[TelegramSpreadRequest](MediaType.application.json)
      .out[String]
      .outErrors(
        HttpCodec.error[TarotErrorResponse](Status.BadRequest),
        HttpCodec.error[TarotErrorResponse](Status.InternalServerError)
      )
      .tag(tag)

  private val postSpreadRoute = postSpreadEndpoint.implement { request =>
    (for {
      _ <- ZIO.logInfo(s"Received request to create spread: ${request.title}")    
      externalSpread <- TelegramSpreadRequest.fromTelegram(request)
      
      spreadCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.spreadCommandHandler)
      spreadCommand = SpreadCommand(externalSpread)
      spreadId <- spreadCommandHandler.handle(spreadCommand)
    } yield spreadId)
      .mapBoth(
        error => TarotErrorMapper.toResponse(error),
        spreadId => spreadId.id.toString)
  }

  private val postCardEndpoint =
    Endpoint(POST / cardRoute)
      .in[TelegramCardRequest](MediaType.application.json)
      .out[String]
      .outErrors(
        HttpCodec.error[TarotErrorResponse](Status.BadRequest),
        HttpCodec.error[TarotErrorResponse](Status.NotFound),
        HttpCodec.error[TarotErrorResponse](Status.InternalServerError)
      )
      .tag(tag)

  private val postCardRoute = postCardEndpoint.implement { case (spreadId, index, request) =>
    (for {
      _ <- ZIO.logInfo(s"Received request to create card number $index for spread $spreadId")
      externalCard <- TelegramCardRequest.fromTelegram(request, index, spreadId)

      cardCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.cardCommandHandler)
      cardCommand = CardCommand(externalCard)
      cardId <- cardCommandHandler.handle(cardCommand)
    } yield cardId)
      .mapBoth(
        error => TarotErrorMapper.toResponse(error),
        cardId => cardId.id.toString)
  }

  val allEndpoints: List[Endpoint[?, ?, ?, ?, ?]] =
    List(postSpreadEndpoint)

  val allRoutes: Routes[AppEnv, Response] =
    Routes(postSpreadRoute)
}
