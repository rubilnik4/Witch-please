package bot.infrastructure.services.tarot

import shared.api.dto.tarot.authorize.*
import shared.api.dto.tarot.projects.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.common.*
import shared.api.dto.tarot.errors.TarotErrorResponse
import shared.api.dto.tarot.users.UserCreateRequest
import shared.models.api.ApiError
import shared.models.telegram.*
import zio.ZIO

import java.util.UUID

trait TarotApiService {
  def createUser(request: UserCreateRequest): ZIO[Any, ApiError, IdResponse]
  def tokenAuth(request: AuthRequest): ZIO[Any, ApiError, AuthResponse]
  def createProject(request: ProjectCreateRequest, token: String): ZIO[Any, ApiError, IdResponse]
  def createSpread(request: TelegramSpreadCreateRequest, token: String): ZIO[Any, ApiError, IdResponse]
  def createCard(request: TelegramCardCreateRequest, spreadId: UUID, index: Int, token: String): ZIO[Any, ApiError, IdResponse]
  def publishSpread(request: SpreadPublishRequest, spreadId: UUID, token: String): ZIO[Any, ApiError, Unit]
}
