package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.cards.CardResponse
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.spreads.*
import shared.models.tarot.authorize.Role
import shared.models.tarot.contracts.TarotChannelType
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.cards.CardResponseMapper
import tarot.api.dto.tarot.spreads.*
import tarot.api.endpoints.errors.TapirError
import tarot.api.infrastructure.AuthValidator
import tarot.application.commands.*
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.SpreadId
import tarot.layers.TarotEnv
import zio.ZIO

import java.util.UUID

object SpreadEndpoint {
  import TapirError.*
  
  private final val tag = "spreads"

  private val getSpreadsEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / "spread" / "by-project" / path[UUID]("projectId"))
      .out(jsonBody[List[SpreadResponse]])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.PreProject)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        projectId =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get spreads by projectId $projectId")

            handler <- ZIO.serviceWith[TarotEnv](_.tarotQueryHandler.spreadsQueryHandler)
            spreads <- handler.getSpreads(ProjectId(projectId))
          } yield spreads.map(SpreadResponseMapper.toResponse)).mapResponseErrors
      }

  private val getSpreadEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / "spread" / path[UUID]("spreadId"))
      .out(jsonBody[SpreadResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.PreProject)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        spreadId =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get spread by spreadId $spreadId")

            handler <- ZIO.serviceWith[TarotEnv](_.tarotQueryHandler.spreadsQueryHandler)
            spread <- handler.getSpread(SpreadId(spreadId))
          } yield SpreadResponseMapper.toResponse(spread)).mapResponseErrors
      }

  private val postSpreadEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.post
      .in(TarotApiRoutes.apiPath / TarotChannelType.Telegram / "spread")
      .in(jsonBody[TelegramSpreadCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        request =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create spread: ${request.title}")
            externalSpread <- TelegramSpreadCreateRequestMapper.fromTelegram(request)
            handler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.spreadCommandHandler)
            spreadId <- handler.createSpread(externalSpread)
          } yield IdResponse(spreadId.id)).mapResponseErrors
      }

  private val deleteSpreadEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.delete
      .in(TarotApiRoutes.apiPath / "spread" / path[UUID]("spreadId"))
      .out(emptyOutput)
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        spreadId =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to delete spread: $spreadId")
            handler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.spreadCommandHandler)
            _ <- handler.deleteSpread(SpreadId(spreadId))
          } yield ()).mapResponseErrors
      }

  
  private val getCardsEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / "card" / "by-spread" / path[UUID]("spreadId"))
      .out(jsonBody[List[CardResponse]])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.PreProject)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        spreadId =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get cards by spreadId $spreadId")

            handler <- ZIO.serviceWith[TarotEnv](_.tarotQueryHandler.cardsQueryHandler)
            cards <- handler.getCards(SpreadId(spreadId))
          } yield cards.map(CardResponseMapper.toResponse)).mapResponseErrors
      }

  private val getCardsCountEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / "card" / "by-spread" / path[UUID]("spreadId") / "count") 
      .out(jsonBody[Int])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.PreProject)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        spreadId =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get cards count by spreadId $spreadId")

            handler <- ZIO.serviceWith[TarotEnv](_.tarotQueryHandler.cardsQueryHandler)
            cardsCount <- handler.getCardsCount(SpreadId(spreadId))
          } yield cardsCount).mapResponseErrors
      }
      
  private val postCardEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.post
      .in(TarotApiRoutes.apiPath / TarotChannelType.Telegram / "spread" / path[UUID]("spreadId") / "cards" / path[Int]("index"))
      .in(jsonBody[TelegramCardCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload => {
        case (spreadId, index, request) =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create card number $index for spread $spreadId")
            externalCard <- TelegramCardCreateRequestMapper.fromTelegram(request, index, spreadId)

            handler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.cardCommandHandler)
            cardId <- handler.createCard(externalCard)
          } yield IdResponse(cardId.id)).mapResponseErrors
        }
      }

  private val publishSpreadEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.put
      .in(TarotApiRoutes.apiPath / "spread" / path[UUID]("spreadId") / "publish")
      .in(jsonBody[SpreadPublishRequest])
      .out(emptyOutput)
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload => {
        case (spreadId, request) =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to publish spread: $spreadId")
            _ <- SpreadPublishRequestMapper.validate(request)
            handler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.spreadCommandHandler)
            _ <- handler.publishSpread(SpreadId(spreadId), request.scheduledAt)
          } yield ()).mapResponseErrors
        }
      }

  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] =
    List(
      getSpreadsEndpoint, getSpreadEndpoint, postSpreadEndpoint, deleteSpreadEndpoint,
      getCardsEndpoint, getCardsCountEndpoint, postCardEndpoint,
      publishSpreadEndpoint
    )
}
