package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.cards.{CardCreateRequest, CardResponse, CardUpdateRequest}
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.spreads.*
import shared.models.tarot.authorize.Role
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.cards.CardResponseMapper
import tarot.api.dto.tarot.spreads.*
import tarot.api.endpoints.errors.TapirError
import tarot.api.infrastructure.AuthValidator
import tarot.application.commands.spreads.commands.ScheduleSpreadCommand
import tarot.domain.models.cards.CardId
import tarot.domain.models.spreads.SpreadId
import tarot.domain.models.users.UserId
import tarot.layers.TarotEnv
import zio.ZIO

import java.util.UUID

object SpreadEndpoint {
  import TapirError.*
  
  private final val tag = "spreads"

  private val getSpreadsEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.spreads)
      .out(jsonBody[List[SpreadResponse]])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        _ =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get spreads by user ${tokenPayload.userId}")
  
            handler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
            spreads <- handler.getSpreads(UserId(tokenPayload.userId))
          } yield spreads.map(SpreadResponseMapper.toResponse)).mapResponseErrors
      }

  private val getSpreadEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.spreads / path[UUID]("spreadId"))
      .out(jsonBody[SpreadResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        spreadId =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get spread by $spreadId")

            handler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
            spread <- handler.getSpread(SpreadId(spreadId))
          } yield SpreadResponseMapper.toResponse(spread)).mapResponseErrors
      }

  private val postSpreadEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.post
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.spreads)
      .in(jsonBody[SpreadCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        request =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create spread: ${request.title}")            
           
            handler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.spreadCommandHandler)
            command <- SpreadRequestMapper.fromRequest(request, UserId(tokenPayload.userId))
            spreadId <- handler.createSpread(command)
          } yield IdResponse(spreadId.id)).mapResponseErrors
      }

  private val putSpreadEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.put
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.spreads / path[UUID]("spreadId"))
      .in(jsonBody[SpreadUpdateRequest])
      .out(emptyOutput)
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        (spreadId, request) =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to update spread: $spreadId")
            
            handler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.spreadCommandHandler)
            command <- SpreadRequestMapper.fromRequest(request, SpreadId(spreadId))
            _ <- handler.updateSpread(command)
          } yield()).mapResponseErrors
      }
      
  private val deleteSpreadEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.delete
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.spreads / path[UUID]("spreadId"))
      .out(emptyOutput)
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        spreadId =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to delete spread: $spreadId")
            
            handler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.spreadCommandHandler)
            _ <- handler.deleteSpread(SpreadId(spreadId))
          } yield()).mapResponseErrors
      }

  private val publishSpreadEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.put
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.spreads / path[UUID]("spreadId") / "publish")
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
           
            handler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.spreadCommandHandler)
            command <- SpreadPublishRequestMapper.fromRequest(request, UserId(tokenPayload.userId), SpreadId(spreadId))
            _ <- handler.scheduleSpread(command)
          } yield ()).mapResponseErrors
        }
      }
  
  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] =
    List(
      getSpreadsEndpoint, getSpreadEndpoint, postSpreadEndpoint,
      putSpreadEndpoint, deleteSpreadEndpoint, publishSpreadEndpoint
    )
}
