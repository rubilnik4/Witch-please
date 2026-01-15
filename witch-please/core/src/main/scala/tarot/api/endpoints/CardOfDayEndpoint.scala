package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.cardsOfDay.*
import shared.api.dto.tarot.common.IdResponse
import shared.models.tarot.authorize.Role
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.cardOfDay.*
import tarot.api.endpoints.errors.TapirError
import tarot.api.infrastructure.AuthValidator
import tarot.domain.models.cardsOfDay.CardOfDayId
import tarot.domain.models.spreads.SpreadId
import tarot.layers.TarotEnv
import zio.ZIO

import java.util.UUID

object CardOfDayEndpoint {
  import TapirError.*
  
  private final val tag = "cards-of-day"

  private val getCardOfDayEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.cardsOfDay / path[UUID]("cardOfDayId"))
      .out(jsonBody[CardOfDayResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        cardOfDayId =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get card of day by id $cardOfDayId")

            handler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardOfDayQueryHandler)
            cardOfDay <- handler.getCardOfDay(CardOfDayId(cardOfDayId))
          } yield CardOfDayResponseMapper.toResponse(cardOfDay)).mapResponseErrors
      }

  private val getCardOfDayBySpreadEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.spreads / path[UUID]("spreadId") / TarotApiRoutes.cardsOfDay)
      .out(jsonBody[Option[CardOfDayResponse]])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        spreadId =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get card of day by spreadId $spreadId")

            handler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardOfDayQueryHandler)
            cardOfDay <- handler.getCardOfDayBySpreadOption(SpreadId(spreadId))
          } yield cardOfDay.map(CardOfDayResponseMapper.toResponse)).mapResponseErrors
      }

  private val postCardOfDayEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.post
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.spreads / path[UUID]("spreadId") / TarotApiRoutes.cardsOfDay)
      .in(jsonBody[CardOfDayCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload => {
        case (spreadId, request) =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create card of day ${request.cardId} for spread $spreadId")

            command <- CardOfDayRequestMapper.fromRequest(request, SpreadId(spreadId))
            handler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.cardOfDayCommandHandler)
            cardOfDayId <- handler.createCardOfDay(command)
          } yield IdResponse(cardOfDayId.id)).mapResponseErrors
        }
      }

  private val putCardOfDayEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.put
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.cardsOfDay / path[UUID]("cardOfDayId"))
      .in(jsonBody[CardOfDayUpdateRequest])
      .out(emptyOutput)
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload => {
        case (cardOfDayId, request) =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to update card of day $cardOfDayId")

            command <- CardOfDayRequestMapper.fromRequest(request, CardOfDayId(cardOfDayId))
            handler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.cardOfDayCommandHandler)
            _ <- handler.updateCardOfDay(command)
          } yield ()).mapResponseErrors
        }
      }

  private val deleteCardOfDayEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.delete
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.cardsOfDay / path[UUID]("cardOfDayId"))
      .out(emptyOutput)
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        cardOfDayId =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to delete card of day: $cardOfDayId")

            handler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.cardOfDayCommandHandler)
            _ <- handler.deleteCardOfDay(CardOfDayId(cardOfDayId))
          } yield ()).mapResponseErrors
      }
      
  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] =
    List(
      getCardOfDayEndpoint, getCardOfDayBySpreadEndpoint,
      postCardOfDayEndpoint, putCardOfDayEndpoint, deleteCardOfDayEndpoint
    )
}
