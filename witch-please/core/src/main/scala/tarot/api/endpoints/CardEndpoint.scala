package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.cards.{CardCreateRequest, CardResponse, CardUpdateRequest}
import shared.api.dto.tarot.common.IdResponse
import shared.models.tarot.authorize.Role
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.cards.CardResponseMapper
import tarot.api.dto.tarot.spreads.*
import tarot.api.endpoints.errors.TapirError
import tarot.api.infrastructure.AuthValidator
import tarot.domain.models.cards.CardId
import tarot.domain.models.spreads.SpreadId
import tarot.layers.TarotEnv
import zio.ZIO

import java.util.UUID

object CardEndpoint {
  import TapirError.*
  
  private final val tag = "cards"

  private val getCardEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.cards / path[UUID]("cardId"))
      .out(jsonBody[CardResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        cardId =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get card by cardId $cardId")

            handler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
            card <- handler.getCard(CardId(cardId))
          } yield CardResponseMapper.toResponse(card)).mapResponseErrors
      }
      
  private val getCardsEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.spreads / path[UUID]("spreadId") / TarotApiRoutes.cards)
      .out(jsonBody[List[CardResponse]])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        spreadId =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get cards by spreadId $spreadId")

            handler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
            cards <- handler.getCards(SpreadId(spreadId))
          } yield cards.map(CardResponseMapper.toResponse)).mapResponseErrors
      }

  private val getCardsCountEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.spreads / path[UUID]("spreadId") / TarotApiRoutes.cards / "count") 
      .out(jsonBody[Int])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        spreadId =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get cards count by spreadId $spreadId")

            handler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.cardQueryHandler)
            cardsCount <- handler.getCardsCount(SpreadId(spreadId))
          } yield cardsCount).mapResponseErrors
      }
      
  private val postCardEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.post
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.spreads / path[UUID]("spreadId") / TarotApiRoutes.cards)
      .in(jsonBody[CardCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload => {
        case (spreadId, request) =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create card ${request.position} for spread $spreadId")
            
            command <- CardRequestMapper.fromRequest(request, SpreadId(spreadId))
            handler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.cardCommandHandler)
            cardId <- handler.createCard(command)
          } yield IdResponse(cardId.id)).mapResponseErrors
        }
      }

  private val putCardEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.put
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.cards / path[UUID]("cardId"))
      .in(jsonBody[CardUpdateRequest])
      .out(emptyOutput)
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload => {
        case (cardId, request) =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to update card $cardId")

            command <- CardRequestMapper.fromRequest(request, CardId(cardId))
            handler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.cardCommandHandler)
            _ <- handler.updateCard(command)
          } yield ()).mapResponseErrors
        }
      }

  private val deleteCardEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.delete
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.cards / path[UUID]("cardId"))
      .out(emptyOutput)
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        cardId =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to delete card: $cardId")

            handler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.cardCommandHandler)
            _ <- handler.deleteCard(CardId(cardId))
          } yield ()).mapResponseErrors
      }
      
  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] =
    List(
      getCardEndpoint, getCardsEndpoint, getCardsCountEndpoint,
      postCardEndpoint, putCardEndpoint, deleteCardEndpoint
    )
}
