package tarot.api.endpoints

import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.errors.TarotErrorResponse
import shared.api.dto.tarot.spreads.*
import shared.models.tarot.authorize.Role
import shared.models.tarot.contracts.TarotChannelType
import sttp.model.StatusCode
import tarot.api.infrastructure.AuthValidator
import tarot.application.commands.*
import tarot.application.commands.spreads.*
import tarot.domain.models.spreads.SpreadId
import tarot.layers.AppEnv
import zio.ZIO
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.generic.auto.*
import tarot.api.dto.tarot.errors.TarotErrorResponseMapper
import tarot.api.dto.tarot.spreads.*

import java.util.UUID

object SpreadEndpoint {
  private final val tag = "spreads"

  val postSpreadEndpoint: ZServerEndpoint[AppEnv, Any] =
    endpoint.post
      .in(ApiPath.apiPath / TarotChannelType.Telegram / "spread")
      .in(jsonBody[TelegramSpreadCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(
        oneOf[TarotErrorResponse](
          oneOfVariant(StatusCode.BadRequest, jsonBody[TarotErrorResponse.BadRequestError]),
          oneOfVariant(StatusCode.InternalServerError, jsonBody[TarotErrorResponse.InternalServerError]),
          oneOfVariant(StatusCode.Unauthorized, jsonBody[TarotErrorResponse.Unauthorized])
        )
      )
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(AuthValidator.verifyToken(Role.Admin))
      .serverLogic { tokenPayload =>
        request =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create spread: ${request.title}")
            externalSpread <- TelegramSpreadCreateRequestMapper.fromTelegram(request)
            handler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.spreadCreateCommandHandler)
            cmd = SpreadCreateCommand(externalSpread)
            spreadId <- handler.handle(cmd)
          } yield IdResponse(spreadId.id))
            .mapError(err => TarotErrorResponseMapper.toResponse(err))
      }

  val postCardEndpoint: ZServerEndpoint[AppEnv, Any] =
    endpoint.post
      .in(ApiPath.apiPath / TarotChannelType.Telegram / "spread" / path[UUID]("spreadId") / "cards" / path[Int]("index"))
      .in(jsonBody[TelegramCardCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(
        oneOf[TarotErrorResponse](
          oneOfVariant(StatusCode.BadRequest, jsonBody[TarotErrorResponse.BadRequestError]),
          oneOfVariant(StatusCode.NotFound, jsonBody[TarotErrorResponse.NotFoundError]),
          oneOfVariant(StatusCode.InternalServerError, jsonBody[TarotErrorResponse.InternalServerError]),
          oneOfVariant(StatusCode.Unauthorized, jsonBody[TarotErrorResponse.Unauthorized])
        )
      )
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(AuthValidator.verifyToken(Role.Admin))
      .serverLogic { tokenPayload => {
        case (spreadId, index, request) =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create card number $index for spread $spreadId")
            externalCard <- TelegramCardCreateRequestMapper.fromTelegram(request, index, spreadId)

            handler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.cardCreateCommandHandler)
            cmd = CardCreateCommand(externalCard)
            cardId <- handler.handle(cmd)
          } yield IdResponse(cardId.id))
            .mapError(err => TarotErrorResponseMapper.toResponse(err))
        }
      }

  val publishSpreadEndpoint: ZServerEndpoint[AppEnv, Any] =
    endpoint.put
      .in(ApiPath.apiPath / "spread" / path[UUID]("spreadId") / "publish")
      .in(jsonBody[SpreadPublishRequest])
      .out(emptyOutput)
      .errorOut(
        oneOf[TarotErrorResponse](
          oneOfVariant(StatusCode.BadRequest, jsonBody[TarotErrorResponse.BadRequestError]),
          oneOfVariant(StatusCode.NotFound, jsonBody[TarotErrorResponse.NotFoundError]),
          oneOfVariant(StatusCode.Conflict, jsonBody[TarotErrorResponse.ConflictError]),
          oneOfVariant(StatusCode.InternalServerError, jsonBody[TarotErrorResponse.InternalServerError]),
          oneOfVariant(StatusCode.Unauthorized, jsonBody[TarotErrorResponse.Unauthorized])
        )
      )
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(AuthValidator.verifyToken(Role.Admin))
      .serverLogic { tokenPayload => {
        case (spreadId, request) =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to publish spread: $spreadId")
            _ <- SpreadPublishRequestMapper.validate(request)
            handler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.spreadPublishCommandHandler)
            cmd = SpreadPublishCommand(SpreadId(spreadId), request.scheduledAt)
            _ <- handler.handle(cmd)
          } yield ())
            .mapError(err => TarotErrorResponseMapper.toResponse(err))
        }
      }

  val endpoints: List[ZServerEndpoint[AppEnv, Any]] =
    List(postSpreadEndpoint, postCardEndpoint, publishSpreadEndpoint)
}
