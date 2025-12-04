package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.users.*
import shared.models.tarot.authorize.{ClientType, Role}
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.*
import tarot.api.dto.tarot.errors.TarotErrorResponseMapper
import tarot.api.dto.tarot.users.*
import tarot.api.endpoints.errors.TapirError
import tarot.api.infrastructure.AuthValidator
import tarot.application.commands.*
import tarot.layers.TarotEnv
import zio.ZIO

object AuthorEndpoint {
  import TapirError.*
  
  private final val tag = "authors"
  
  private val getAuthorsEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.authors)
      .out(jsonBody[List[AuthorResponse]])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)    
      .zServerLogic { request =>
        (for {
          _ <- ZIO.logInfo(s"Received request to get authors")

          userQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.userQueryHandler)
          authors <- userQueryHandler.getAuthors
        } yield authors.map(AuthorResponseMapper.toResponse)).mapResponseErrors
      }

  private val postAuthorEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .post
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.authors)
      .in(jsonBody[UserCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .zServerLogic { request =>
        (for {
          _ <- ZIO.logInfo(s"Received request to create user: ${request.name}")

          externalUser <- UserCreateRequestMapper.fromRequest(request, ClientType.Telegram)
          userCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.userCommandHandler)
          userId <- userCommandHandler.createAuthor(externalUser)
          
        } yield IdResponse(userId.id)).mapResponseErrors
      }

  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] = 
    List(postAuthorEndpoint, getAuthorsEndpoint)
}
