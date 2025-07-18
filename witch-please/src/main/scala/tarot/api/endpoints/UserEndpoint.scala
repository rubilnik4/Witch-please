package tarot.api.endpoints

import tarot.api.dto.tarot.*
import tarot.api.dto.tarot.spreads.*
import tarot.api.dto.tarot.users.UserCreateRequest
import tarot.api.middlewares.AuthMiddleware
import tarot.application.commands.*
import tarot.application.commands.users.UserCreateCommand
import tarot.application.commands.spreads.{CardCreateCommand, SpreadCreateCommand, SpreadPublishCommand}
import tarot.domain.models.auth.{ClientType, Role}
import tarot.domain.models.contracts.TarotChannelType
import tarot.domain.models.spreads.SpreadId
import tarot.layers.AppEnv
import zio.ZIO
import zio.http.*
import zio.http.Method.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint

object UserEndpoint {
  private final val user = "user"
  private final val userTag = "users"

  private final val userCreatePath =
    Root / PathBuilder.apiPath / TarotChannelType.Telegram / user

  private val postUserEndpoint =
    Endpoint(POST / userCreatePath)
      .in[UserCreateRequest](MediaType.application.json)
      .out[String]
      .outErrors(
        HttpCodec.error[TarotErrorResponse](Status.BadRequest),
        HttpCodec.error[TarotErrorResponse](Status.InternalServerError)
      )
      .tag(userTag)

  private val postUserRoute = postUserEndpoint.implement { request =>
    (for {
      _ <- ZIO.logInfo(s"Received request to create user: ${request.name}")
      externalUser <- UserCreateRequest.fromRequest(request, ClientType.Telegram)

      userCreateCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.userCreateCommandHandler)
      userCreateCommand = UserCreateCommand(externalUser)
      userId <- userCreateCommandHandler.handle(userCreateCommand)
    } yield userId)
      .mapBoth(
        error => TarotErrorResponse.toResponse(error),
        userId => userId.id.toString)
  }

  val allEndpoints: List[Endpoint[?, ?, ?, ?, ?]] =
    List(postUserEndpoint)

  val allRoutes: Routes[AppEnv, Response] =
    Routes(postUserRoute)
}
