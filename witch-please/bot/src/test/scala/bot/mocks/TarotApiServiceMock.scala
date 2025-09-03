package bot.mocks

import bot.infrastructure.services.tarot.TarotApiService
import shared.api.dto.tarot.authorize.*
import shared.api.dto.tarot.common.*
import shared.api.dto.tarot.projects.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
import shared.infrastructure.services.common.DateTimeService
import shared.models.api.ApiError
import shared.models.tarot.authorize.ClientType
import shared.models.tarot.authorize.Role.PreProject
import sttp.model.StatusCode
import zio.*

import java.time.Instant
import java.util.UUID

final class TarotApiServiceMock(ref: Ref.Synchronized[Map[String, UserResponse]]) extends TarotApiService {
  def createUser(request: UserCreateRequest): ZIO[Any, ApiError, IdResponse] =
    for {
      now <- DateTimeService.getDateTimeNow
      res <- ref.modifyZIO { users =>
        users.get(request.clientId) match {
          case Some(_) =>
            ZIO.fail(ApiError.HttpCode(StatusCode.Conflict.code, s"user ${request.clientId} already exists"))
          case None =>
            val user = getUserResponse(UUID.randomUUID(), request, now)
            ZIO.succeed((IdResponse(user.id), users.updated(request.clientId, user)))
        }
      }
    } yield res
  
  def getUserByClientId(clientId: String): ZIO[Any, ApiError, UserResponse] =
    ref.get.map(_.get(clientId)).flatMap {
      case Some(u) => ZIO.succeed(u)
      case None => ZIO.fail(ApiError.HttpCode(StatusCode.NotFound.code, s"user $clientId not found"))
    }  
  
  def getOrCreateUserId(request: UserCreateRequest): ZIO[Any, ApiError, UUID] =
    getUserByClientId(request.clientId).map(_.id).catchSome {
      case ApiError.HttpCode(code, _) if code == StatusCode.NotFound.code =>
        createUser(request).map(_.id)
    }  
  
  def tokenAuth(request: AuthRequest): ZIO[Any, ApiError, AuthResponse] =
    ZIO.succeed(AuthResponse(token = "test-token", role = PreProject))
  
  def createProject(request: ProjectCreateRequest, token: String): ZIO[Any, ApiError, IdResponse] =
    ZIO.succeed(IdResponse(UUID.randomUUID()))
  
  def createSpread(request: TelegramSpreadCreateRequest, token: String): ZIO[Any, ApiError, IdResponse] =
    ZIO.succeed(IdResponse(UUID.randomUUID()))
  
  def createCard(request: TelegramCardCreateRequest, spreadId: UUID, index: Int, token: String): ZIO[Any, ApiError, IdResponse] =
    ZIO.succeed(IdResponse(UUID.randomUUID()))
  
  def publishSpread(request: SpreadPublishRequest, spreadId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    ZIO.unit     
  
  private def getUserResponse(id: UUID, request: UserCreateRequest, now: Instant) =
    UserResponse(
      id = id,
      clientId = request.clientId,
      clientType = ClientType.Telegram,
      name = request.name,
      createdAt = now
    )
}

object TarotApiServiceMock {
  val tarotApiServiceLive: ULayer[TarotApiService] =
    ZLayer.fromZIO(Ref.Synchronized.make(Map.empty[String, UserResponse]).map(new TarotApiServiceMock(_)))
}
