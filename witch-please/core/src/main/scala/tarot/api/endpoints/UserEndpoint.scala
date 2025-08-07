package tarot.api.endpoints

import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.common.IdResponse
import tarot.api.dto.tarot.*
import tarot.api.dto.tarot.users.UserCreateRequest
import tarot.application.commands.*
import tarot.application.commands.users.UserCreateCommand
import tarot.domain.models.authorize.ClientType
import tarot.domain.models.contracts.TarotChannelType
import tarot.layers.AppEnv
import zio.ZIO

object UserEndpoint {
  private final val tag = "users"

  val postUserEndpoint: ZServerEndpoint[AppEnv, Any] =
    endpoint
      .post
      .in(ApiPath.apiPath / TarotChannelType.Telegram / "user")
      .in(jsonBody[UserCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(
        oneOf[TarotErrorResponse](
          oneOfVariant(StatusCode.BadRequest, jsonBody[TarotErrorResponse]),
          oneOfVariant(StatusCode.InternalServerError, jsonBody[TarotErrorResponse]),
          oneOfVariant(StatusCode.Conflict, jsonBody[TarotErrorResponse]),
        )
      )
      .tag(tag)
      .zServerLogic { request =>
        (for {
          _ <- ZIO.logInfo(s"Received request to create user: ${request.name}")
          externalUser <- UserCreateRequest.fromRequest(request, ClientType.Telegram)
          userCreateCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.userCreateCommandHandler)
          userCreateCommand = UserCreateCommand(externalUser)
          userId <- userCreateCommandHandler.handle(userCreateCommand)
        } yield IdResponse(userId.id))
          .mapError(TarotErrorResponse.toResponse)
      }

  val endpoints: List[ZServerEndpoint[AppEnv, Any]] = 
    List(postUserEndpoint)  
}
