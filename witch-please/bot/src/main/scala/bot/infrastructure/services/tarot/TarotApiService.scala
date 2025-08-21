package bot.infrastructure.services.tarot

import shared.api.dto.tarot.authorize.*
import shared.api.dto.tarot.projects.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.common.*
import shared.api.dto.tarot.errors.TarotErrorResponse
import shared.api.dto.tarot.users.UserCreateRequest
import shared.models.telegram.*
import zio.ZIO

trait TarotApiService {
  def createUser(request: UserCreateRequest): ZIO[Any, TarotErrorResponse, IdResponse]
  def tokenAuth(request: AuthRequest): ZIO[Any, TarotErrorResponse, AuthResponse]
  def createProject(request: ProjectCreateRequest, token: String): ZIO[Any, Throwable, IdResponse]
  def createSpread(request: TelegramSpreadCreateRequest): ZIO[Any, Throwable, IdResponse]
}
