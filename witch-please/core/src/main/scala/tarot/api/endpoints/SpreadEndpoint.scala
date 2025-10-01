package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.spreads.*
import shared.models.tarot.authorize.Role
import shared.models.tarot.contracts.TarotChannelType
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.errors.TarotErrorResponseMapper
import tarot.api.dto.tarot.spreads.*
import tarot.api.endpoints.errors.TapirError
import tarot.api.infrastructure.AuthValidator
import tarot.application.commands.*
import tarot.application.commands.spreads.*
import tarot.domain.models.spreads.SpreadId
import tarot.layers.TarotEnv
import zio.ZIO

import java.util.UUID

object SpreadEndpoint {
  private final val tag = "spreads"

  private val postSpreadEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.post
      .in(TarotApiRoutes.apiPath / TarotChannelType.Telegram / "spread")
      .in(jsonBody[TelegramSpreadCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(AuthValidator.verifyToken(Role.Admin))
      .serverLogic { tokenPayload =>
        request =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create spread: ${request.title}")
            externalSpread <- TelegramSpreadCreateRequestMapper.fromTelegram(request)
            handler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.spreadCreateCommandHandler)
            cmd = SpreadCreateCommand(externalSpread)
            spreadId <- handler.handle(cmd)
          } yield IdResponse(spreadId.id))
            .mapError(err => TarotErrorResponseMapper.toResponse(err))
      }

  private val postCardEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.post
      .in(TarotApiRoutes.apiPath / TarotChannelType.Telegram / "spread" / path[UUID]("spreadId") / "cards" / path[Int]("index"))
      .in(jsonBody[TelegramCardCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(AuthValidator.verifyToken(Role.Admin))
      .serverLogic { tokenPayload => {
        case (spreadId, index, request) =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create card number $index for spread $spreadId")
            externalCard <- TelegramCardCreateRequestMapper.fromTelegram(request, index, spreadId)

            handler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.cardCreateCommandHandler)
            cmd = CardCreateCommand(externalCard)
            cardId <- handler.handle(cmd)
          } yield IdResponse(cardId.id))
            .mapError(err => TarotErrorResponseMapper.toResponse(err))
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
      .zServerSecurityLogic(AuthValidator.verifyToken(Role.Admin))
      .serverLogic { tokenPayload => {
        case (spreadId, request) =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to publish spread: $spreadId")
            _ <- SpreadPublishRequestMapper.validate(request)
            handler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.spreadPublishCommandHandler)
            cmd = SpreadPublishCommand(SpreadId(spreadId), request.scheduledAt)
            _ <- handler.handle(cmd)
          } yield ())
            .mapError(err => TarotErrorResponseMapper.toResponse(err))
        }
      }

  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] =
    List(postSpreadEndpoint, postCardEndpoint, publishSpreadEndpoint)
}
