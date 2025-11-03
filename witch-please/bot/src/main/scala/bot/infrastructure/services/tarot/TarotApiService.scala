package bot.infrastructure.services.tarot

import shared.api.dto.tarot.authorize.*
import shared.api.dto.tarot.cards.CardResponse
import shared.api.dto.tarot.common.*
import shared.api.dto.tarot.projects.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
import shared.models.api.ApiError
import zio.ZIO

import java.util.UUID

trait TarotApiService {
  def createUser(request: UserCreateRequest): ZIO[Any, ApiError, IdResponse]
  def getUserByClientId(clientId: String): ZIO[Any, ApiError, UserResponse]
  def getOrCreateUserId(request: UserCreateRequest): ZIO[Any, ApiError, UUID]
  def tokenAuth(request: AuthRequest): ZIO[Any, ApiError, AuthResponse]
  def createProject(request: ProjectCreateRequest, token: String): ZIO[Any, ApiError, IdResponse]
  def getProjects(userId: UUID, token: String): ZIO[Any, ApiError, List[ProjectResponse]]  
  def createSpread(request: TelegramSpreadCreateRequest, token: String): ZIO[Any, ApiError, IdResponse]
  def getSpread(spreadId: UUID, token: String): ZIO[Any, ApiError, SpreadResponse]
  def getSpreads(projectId: UUID, token: String): ZIO[Any, ApiError, List[SpreadResponse]]
  def getCards(spreadId: UUID, token: String): ZIO[Any, ApiError, List[CardResponse]]
  def getCardsCount(spreadId: UUID, token: String): ZIO[Any, ApiError, Int]
  def createCard(request: TelegramCardCreateRequest, spreadId: UUID, index: Int, token: String): ZIO[Any, ApiError, IdResponse]
  def publishSpread(request: SpreadPublishRequest, spreadId: UUID, token: String): ZIO[Any, ApiError, Unit]
}
