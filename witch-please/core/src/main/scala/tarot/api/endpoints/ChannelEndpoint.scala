package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.channels.*
import shared.api.dto.tarot.common.IdResponse
import shared.models.tarot.authorize.Role
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.channels.*
import tarot.api.endpoints.errors.TapirError
import tarot.api.infrastructure.AuthValidator
import tarot.domain.models.channels.UserChannelId
import tarot.domain.models.users.UserId
import tarot.layers.TarotEnv
import zio.ZIO

import java.util.UUID

object ChannelEndpoint {
  import TapirError.*

  private final val tag = "channels"

  private val postChannelEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .post
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.channels)
      .in(jsonBody[ChannelCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        request =>
          (for {
            _ <- ZIO.logInfo(s"Received request to create channel: ${request.channelId} by user: ${tokenPayload.userId}")

            handler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.userChannelCommandHandler)
            command <- ChannelRequestMapper.fromRequest(request, UserId(tokenPayload.userId))
            userChannelId <- handler.createUserChannel(command)
          } yield IdResponse(userChannelId.id)).mapResponseErrors
      }

  private val putChannelEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .put
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.channels / path[UUID]("userChannelId"))
      .in(jsonBody[ChannelUpdateRequest])
      .out(emptyOutput)
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload => {
        case (userChannelId, request) =>
          (for {
            _ <- ZIO.logInfo(s"Received request to update user channel: $userChannelId by user: ${tokenPayload.userId}")

            handler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.userChannelCommandHandler)
            command <- ChannelRequestMapper.fromRequest(request, UserChannelId(userChannelId))
            _ <- handler.updateUserChannel(command)
          } yield ()).mapResponseErrors
        }
      }

  private val getDefaultChannelEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.channels / "default")
      .out(jsonBody[Option[UserChannelResponse]])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        _ =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get default channel by user ${tokenPayload.userId}")

            userChannelQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.userChannelQueryHandler)
            userChannel <- userChannelQueryHandler.getUserChannelByUser(UserId(tokenPayload.userId))
          } yield userChannel.map(UserChannelResponseMapper.toResponse)).mapResponseErrors
      }

  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] =
    List(postChannelEndpoint, putChannelEndpoint, getDefaultChannelEndpoint)
}
