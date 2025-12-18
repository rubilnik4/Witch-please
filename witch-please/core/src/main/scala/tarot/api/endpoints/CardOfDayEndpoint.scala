package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.cards.{CardCreateRequest, CardOfDayCreateRequest, CardResponse, CardUpdateRequest}
import shared.api.dto.tarot.common.IdResponse
import shared.models.tarot.authorize.Role
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.cardOfDay.CardOfDayRequestMapper
import tarot.api.dto.tarot.cards.{CardRequestMapper, CardResponseMapper}
import tarot.api.dto.tarot.spreads.*
import tarot.api.endpoints.errors.TapirError
import tarot.api.infrastructure.AuthValidator
import tarot.domain.models.cards.CardId
import tarot.domain.models.spreads.SpreadId
import tarot.layers.TarotEnv
import zio.ZIO

import java.util.UUID

object CardOfDayEndpoint {
  import TapirError.*
  
  private final val tag = "cardsOfDay"
 
  private val postCardOfDayEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint.post
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.spreads / path[UUID]("spreadId") / TarotApiRoutes.cardOfDay)
      .in(jsonBody[CardOfDayCreateRequest])
      .out(jsonBody[IdResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload => {
        case (spreadId, request) =>
          (for {
            _ <- ZIO.logInfo(s"User ${tokenPayload.userId} requested to create card of day ${request.cardId} for spread $spreadId")

            command <- CardOfDayRequestMapper.fromRequest(request, SpreadId(spreadId))
            handler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.cardOfDayCommandHandler)
            cardOfDayId <- handler.createCardOfDay(command)
          } yield IdResponse(cardOfDayId.id)).mapResponseErrors
        }
      }
      
  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] =
    List(
      postCardOfDayEndpoint
    )
}
