package bot.infrastructure.services.tarot

import shared.api.dto.tarot.authorize.*
import shared.api.dto.tarot.cards.*
import shared.api.dto.tarot.cardsOfDay.*
import shared.api.dto.tarot.channels.*
import shared.api.dto.tarot.common.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
import shared.models.api.ApiError
import zio.ZIO

import java.util.UUID

trait TarotApiService {
  def createUser(request: UserCreateRequest): ZIO[Any, ApiError, IdResponse]
  def getUserByClientId(clientId: String): ZIO[Any, ApiError, UserResponse]
  def getOrCreateUserId(request: UserCreateRequest): ZIO[Any, ApiError, IdResponse]
  def getAuthors: ZIO[Any, ApiError, List[AuthorResponse]]
  def tokenAuth(request: AuthRequest): ZIO[Any, ApiError, AuthResponse]
  def getDefaultChannel(token: String): ZIO[Any, ApiError, Option[UserChannelResponse]]
  def createChannel(request: ChannelCreateRequest, token: String): ZIO[Any, ApiError, IdResponse]
  def updateChannel(request: ChannelUpdateRequest, userChannelId: UUID, token: String): ZIO[Any, ApiError, Unit]
  def getSpread(spreadId: UUID, token: String): ZIO[Any, ApiError, SpreadResponse]
  def getSpreads(token: String): ZIO[Any, ApiError, List[SpreadResponse]]
  def createSpread(request: SpreadCreateRequest, token: String): ZIO[Any, ApiError, IdResponse]
  def updateSpread(request: SpreadUpdateRequest, spreadId: UUID, token: String): ZIO[Any, ApiError, Unit]
  def deleteSpread(spreadId: UUID, token: String): ZIO[Any, ApiError, Unit]
  def publishSpread(request: SpreadPublishRequest, spreadId: UUID, token: String): ZIO[Any, ApiError, Unit]
  def getCard(cardId: UUID, token: String): ZIO[Any, ApiError, CardResponse]
  def getCards(spreadId: UUID, token: String): ZIO[Any, ApiError, List[CardResponse]]
  def getCardsCount(spreadId: UUID, token: String): ZIO[Any, ApiError, Int]
  def createCard(request: CardCreateRequest, spreadId: UUID, position: Int, token: String): ZIO[Any, ApiError, IdResponse]
  def updateCard(request: CardUpdateRequest, cardId: UUID, token: String): ZIO[Any, ApiError, Unit]
  def deleteCard(cardId: UUID, token: String): ZIO[Any, ApiError, Unit]
  def getCardOfDayBySpread(spreadId: UUID, token: String): ZIO[Any, ApiError, Option[CardOfDayResponse]]
  def createCardOfDay(request: CardOfDayCreateRequest, spreadId: UUID, token: String): ZIO[Any, ApiError, IdResponse]
  def updateCardOfDay(request: CardOfDayUpdateRequest, cardOfDayId: UUID, token: String): ZIO[Any, ApiError, Unit]
  def deleteCardOfDay(cardOfDayId: UUID, token: String): ZIO[Any, ApiError, Unit]
}
