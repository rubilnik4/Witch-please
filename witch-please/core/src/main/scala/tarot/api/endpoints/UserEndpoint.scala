package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.errors.TarotErrorResponse
import shared.api.dto.tarot.users.UserCreateRequest
import shared.models.tarot.authorize.ClientType
import shared.models.tarot.contracts.TarotChannelType
import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.*
import tarot.api.dto.tarot.errors.TarotErrorResponseMapper
import tarot.api.dto.tarot.users.*
import tarot.application.commands.*
import tarot.application.commands.users.UserCreateCommand
import tarot.layers.AppEnv
import zio.ZIO

object UserEndpoint {
  private final val tag = "users"

  val postUserEndpoint: ZServerEndpoint[AppEnv, Any] =
    endpoint
      .post
      .in(TarotApiRoutes.apiPath / TarotChannelType.Telegram / "user")
      .in(jsonBody[UserCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(
        oneOf[TarotErrorResponse](
          oneOfVariant(StatusCode.BadRequest, jsonBody[TarotErrorResponse.BadRequestError]),
          oneOfVariant(StatusCode.InternalServerError, jsonBody[TarotErrorResponse.InternalServerError]),
          oneOfVariant(StatusCode.Conflict, jsonBody[TarotErrorResponse.ConflictError]),
        )
      )
      .tag(tag)
      .zServerLogic { request =>
        (for {
          _ <- ZIO.logInfo(s"Received request to create user: ${request.name}")
          externalUser <- UserCreateRequestMapper.fromRequest(request, ClientType.Telegram)
          userCreateCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.userCreateCommandHandler)
          userCreateCommand = UserCreateCommand(externalUser)
          userId <- userCreateCommandHandler.handle(userCreateCommand)
        } yield IdResponse(userId.id))
          .mapError(TarotErrorResponseMapper.toResponse)
      }

  val endpoints: List[ZServerEndpoint[AppEnv, Any]] = 
    List(postUserEndpoint)  
}
