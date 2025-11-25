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
import tarot.api.dto.tarot.users.*
import tarot.api.endpoints.errors.TapirError
import tarot.layers.TarotEnv
import zio.ZIO

object UserEndpoint {
  import TapirError.*
  
  private final val tag = "users"

  private val getUserEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / TarotChannelType.Telegram / "user" / "by-client" / path[String]("clientId"))
      .out(jsonBody[UserResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .zServerLogic { clientId =>
        (for {
          _ <- ZIO.logInfo(s"Received request to get user by clientId $clientId")
          
          userQueryHandler <- ZIO.serviceWith[TarotEnv](_.tarotQueryHandler.userQueryHandler)
          user <- userQueryHandler.getUserByClientId(clientId)
        } yield UserResponseMapper.toResponse(user)).mapResponseErrors
      }
      
  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] = 
    List(getUserEndpoint)
}
