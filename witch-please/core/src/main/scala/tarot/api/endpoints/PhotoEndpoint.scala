package tarot.api.endpoints

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.cards.{CardCreateRequest, CardResponse, CardUpdateRequest}
import shared.api.dto.tarot.cardsOfDay.CardOfDayCreateRequest
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.photo.PhotoResponse
import shared.models.tarot.authorize.Role
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import tarot.api.dto.tarot.cardOfDay.CardOfDayRequestMapper
import tarot.api.dto.tarot.cards.{CardRequestMapper, CardResponseMapper}
import tarot.api.dto.tarot.photo.PhotoResponseMapper
import tarot.api.dto.tarot.spreads.*
import tarot.api.endpoints.errors.TapirError
import tarot.api.infrastructure.AuthValidator
import tarot.domain.models.cards.CardId
import tarot.domain.models.photo.PhotoId
import tarot.domain.models.spreads.SpreadId
import tarot.layers.TarotEnv
import zio.ZIO

import java.util.UUID

object PhotoEndpoint {
  import TapirError.*
  
  private final val tag = "photos"

  private val getPhotoEndpoint: ZServerEndpoint[TarotEnv, Any] =
    endpoint
      .get
      .in(TarotApiRoutes.apiPath / TarotApiRoutes.photos / path[UUID]("photoId"))
      .out(jsonBody[PhotoResponse])
      .errorOut(TapirError.tapirErrorOut)
      .tag(tag)
      .securityIn(auth.bearer[String]())
      .zServerSecurityLogic(token => AuthValidator.verifyToken(Role.Admin)(token).mapResponseErrors)
      .serverLogic { tokenPayload =>
        photoId =>
          (for {
            _ <- ZIO.logInfo(s"Received request to get photo by photoId $photoId")

            handler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.photoQueryHandler)
            photo <- handler.getPhoto(PhotoId(photoId))
          } yield PhotoResponseMapper.toResponse(photo)).mapResponseErrors
      }
      
  val endpoints: List[ZServerEndpoint[TarotEnv, Any]] =
    List(
      getPhotoEndpoint
    )
}
