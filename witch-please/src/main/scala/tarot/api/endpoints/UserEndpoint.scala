package tarot.api.endpoints

import sttp.model.StatusCode
import tarot.api.dto.tarot.*
import tarot.api.dto.tarot.spreads.*
import tarot.api.dto.tarot.users.UserCreateRequest
import tarot.api.infrastructure.AuthValidator
import tarot.application.commands.*
import tarot.application.commands.users.UserCreateCommand
import tarot.application.commands.spreads.{CardCreateCommand, SpreadCreateCommand, SpreadPublishCommand}
import tarot.domain.models.authorize.{ClientType, Role}
import tarot.domain.models.contracts.TarotChannelType
import tarot.domain.models.spreads.SpreadId
import tarot.layers.AppEnv
import zio.ZIO
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.generic.auto.*

object UserEndpoint {
  private final val tag = "users"

  private val postUserEndpoint: ZServerEndpoint[AppEnv, Any] =
    endpoint
      .post
      .in(ApiPath.apiPath / TarotChannelType.Telegram / "user")
      .in(jsonBody[UserCreateRequest])
      .out(stringBody)
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
        } yield userId.id.toString)
          .mapError(TarotErrorResponse.toResponse)
      }

  val endpoints: List[ZServerEndpoint[AppEnv, Any]] = 
    List(postUserEndpoint)  
}
