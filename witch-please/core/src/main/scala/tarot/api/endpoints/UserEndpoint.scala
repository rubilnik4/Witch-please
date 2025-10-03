package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.users.*
import shared.models.tarot.authorize.ClientType
import shared.models.tarot.contracts.TarotChannelType
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.*
import tarot.api.dto.tarot.errors.TarotErrorResponseMapper
import tarot.api.dto.tarot.users.*
import tarot.api.endpoints.errors.TapirError
import tarot.application.commands.*
import tarot.application.commands.users.UserCreateCommand
import tarot.application.queries.users.UserByClientIdQuery
import tarot.layers.TarotEnv
import zio.ZIO

object UserEndpoint {
  import TapirError._
  
  private final val tag = "users"

  private val getUserEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.get
      .in(TarotApiRoutes.apiPath / TarotChannelType.Telegram / "user" / "by-client" / path[String]("clientId"))
      .out(jsonBody[UserResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .zServerLogic { clientId =>
        (for {
          _ <- ZIO.logInfo(s"Received request to get user by clientId $clientId")
          
          userQueryHandler <- ZIO.serviceWith[TarotEnv](_.tarotQueryHandler.userByClientIdQueryHandler)
          userQuery = UserByClientIdQuery(clientId)
          user <- userQueryHandler.handle(userQuery)
        } yield UserResponseMapper.toResponse(user)).mapResponseErrors
      }

  private val postUserEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .post
      .in(TarotApiRoutes.apiPath / TarotChannelType.Telegram / "user")
      .in(jsonBody[UserCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .zServerLogic { request =>
        (for {
          _ <- ZIO.logInfo(s"Received request to create user: ${request.name}")
          externalUser <- UserCreateRequestMapper.fromRequest(request, ClientType.Telegram)
          userCreateCommandHandler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.userCreateCommandHandler)
          userCreateCommand = UserCreateCommand(externalUser)
          userId <- userCreateCommandHandler.handle(userCreateCommand)
        } yield IdResponse(userId.id)).mapResponseErrors
      }

  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] = 
    List(getUserEndpoint, postUserEndpoint)
}
