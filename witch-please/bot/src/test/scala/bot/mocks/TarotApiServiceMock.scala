package bot.mocks

import bot.infrastructure.services.tarot.TarotApiService
import shared.api.dto.tarot.authorize.*
import shared.api.dto.tarot.cards.CardResponse
import shared.api.dto.tarot.common.*
import shared.api.dto.tarot.photo.PhotoResponse
import shared.api.dto.tarot.projects.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
import shared.infrastructure.services.common.DateTimeService
import shared.models.api.ApiError
import shared.models.tarot.authorize.{ClientType, Role}
import shared.models.tarot.authorize.Role.PreProject
import shared.models.tarot.photo.PhotoOwnerType
import shared.models.tarot.spreads.SpreadStatus
import sttp.model.StatusCode
import tarot.api.dto.tarot.authorize.TokenPayload
import zio.*
import zio.json.*

import java.time.Instant
import java.util.UUID

final class TarotApiServiceMock(
    userMap: Ref.Synchronized[Map[String, UserResponse]],
    projectMap: Ref.Synchronized[Map[UUID, Map[UUID, ProjectResponse]]],
    spreadMap: Ref.Synchronized[Map[UUID, Map[UUID, SpreadResponse]]],
    cardMap: Ref.Synchronized[Map[UUID, Map[UUID, CardResponse]]]
  ) extends TarotApiService {
  def createUser(request: UserCreateRequest): ZIO[Any, ApiError, IdResponse] =
    for {
      now <- DateTimeService.getDateTimeNow
      idResponse <- userMap.modifyZIO { users =>
        users.get(request.clientId) match {
          case Some(_) =>
            ZIO.fail(ApiError.HttpCode(StatusCode.Conflict.code, s"user ${request.clientId} already exists"))
          case None =>
            val user = getUserResponse(UUID.randomUUID(), request, now)
            ZIO.succeed((IdResponse(user.id), users.updated(request.clientId, user)))
        }
      }
    } yield idResponse
  
  def getUserByClientId(clientId: String): ZIO[Any, ApiError, UserResponse] =
    userMap.get.map(_.get(clientId)).flatMap {
      case Some(user) => ZIO.succeed(user)
      case None => ZIO.fail(ApiError.HttpCode(StatusCode.NotFound.code, s"user $clientId not found"))
    }  
  
  def getOrCreateUserId(request: UserCreateRequest): ZIO[Any, ApiError, UUID] =
    getUserByClientId(request.clientId).map(_.id).catchSome {
      case ApiError.HttpCode(code, _) if code == StatusCode.NotFound.code =>
        createUser(request).map(_.id)
    }  
  
  def tokenAuth(request: AuthRequest): ZIO[Any, ApiError, AuthResponse] =
    val role = if (request.projectId.isDefined) Role.Admin else Role.PreProject
    val tokenPayload = TokenPayload(
      clientType = request.clientType,
      userId = request.userId,
      projectId = request.projectId,
      role = role
    )
    val token = tokenPayload.toJson
    ZIO.succeed(AuthResponse(token = token, role = role))
  
  def createProject(request: ProjectCreateRequest, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      now <- DateTimeService.getDateTimeNow
      payload <- validateToken(token)
      projectId = UUID.randomUUID()
      project = getProjectResponse(projectId, request, now)
      idResponse <- projectMap.modifyZIO { projects =>
        val userProjects = projects.getOrElse(payload.userId, Map.empty)
        val updatedUserProjects = userProjects.updated(projectId, project)
        val updatedProjects = projects.updated(payload.userId, updatedUserProjects)
        ZIO.succeed(IdResponse(projectId), updatedProjects)
      }
    } yield idResponse

  def getProjects(userId: UUID, token: String): ZIO[Any, ApiError, List[ProjectResponse]] =
    projectMap.get.map(_.get(userId)).flatMap {
      case Some(userProjects) => ZIO.succeed(userProjects.values.toList)
      case None => ZIO.fail(ApiError.HttpCode(StatusCode.NotFound.code, s"projects by userId $userId not found"))
    }

  def createSpread(request: TelegramSpreadCreateRequest, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      now <- DateTimeService.getDateTimeNow
      spreadId = UUID.randomUUID()
      spread = getSpreadResponse(spreadId, request, now)
      idResponse <- spreadMap.modifyZIO { spreads =>
        val projectSpreads = spreads.getOrElse(request.projectId, Map.empty)
        val updatedProjectSpreads = projectSpreads.updated(spreadId, spread)
        val updatedSpreads = spreads.updated(request.projectId, updatedProjectSpreads)
        ZIO.succeed(IdResponse(spreadId), updatedSpreads)
      }
    } yield idResponse

  def getSpreads(projectId: UUID, token: String): ZIO[Any, ApiError, List[SpreadResponse]] =
    spreadMap.get.map(_.get(projectId)).flatMap {
      case Some(spreads) => ZIO.succeed(spreads.values.toList)
      case None => ZIO.fail(ApiError.HttpCode(StatusCode.NotFound.code, s"spreads by projectId $projectId not found"))
    }

  def createCard(request: TelegramCardCreateRequest, spreadId: UUID, index: Int, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      now <- DateTimeService.getDateTimeNow
      cardId = UUID.randomUUID()
      card = getCardResponse(cardId, request, spreadId, now)
      idResponse <- cardMap.modifyZIO { cards =>
        val spreadCards = cards.getOrElse(spreadId, Map.empty)
        val updatedSpreadCards = spreadCards.updated(cardId, card)
        val updatedCards = cards.updated(spreadId, updatedSpreadCards)
        ZIO.succeed(IdResponse(cardId), updatedCards)
      }
    } yield idResponse

  def getCards(spreadId: UUID, token: String): ZIO[Any, ApiError, List[CardResponse]] =
    cardMap.get.map(_.get(spreadId)).flatMap {
      case Some(cards) => ZIO.succeed(cards.values.toList)
      case None => ZIO.fail(ApiError.HttpCode(StatusCode.NotFound.code, s"cards by spreadId $spreadId not found"))
    }

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

  private def getProjectResponse(id: UUID, request: ProjectCreateRequest, now: Instant) =
    ProjectResponse(
      id = id,
      name = request.name,
      createdAt = now
    )

  private def getSpreadResponse(id: UUID, request: TelegramSpreadCreateRequest, now: Instant) =
    SpreadResponse(
      id = id,
      projectId = request.projectId,
      title = request.title,
      cardCount = request.cardCount,
      spreadStatus = SpreadStatus.Draft,
      photo = PhotoResponse(PhotoOwnerType.Spread, id, Some(request.coverPhotoId)),
      createdAt =  now,
      scheduledAt = None,
      publishedAt = None
    )

  private def getCardResponse(id: UUID, request: TelegramCardCreateRequest, spreadId: UUID, now: Instant) =
    CardResponse(
      id = id,
      spreadId = spreadId,
      description = request.description,
      photo = PhotoResponse(PhotoOwnerType.Spread, id, Some(request.coverPhotoId)),
      createdAt = now
    )

  private def validateToken(token: String): ZIO[Any, ApiError, TokenPayload] =
    ZIO.fromEither(token.fromJson[TokenPayload])
      .mapError(error => ApiError.InvalidResponse(token, error))
}

object TarotApiServiceMock {
  val tarotApiServiceLive: ULayer[TarotApiService] =
    ZLayer.fromZIO {
      for {
        usersRef <- Ref.Synchronized.make(Map.empty[String, UserResponse])
        projectsRef <- Ref.Synchronized.make(Map.empty[UUID, Map[UUID, ProjectResponse]])
        spreadsRef <- Ref.Synchronized.make(Map.empty[UUID, Map[UUID, SpreadResponse]])
        cardsRef <- Ref.Synchronized.make(Map.empty[UUID, Map[UUID, CardResponse]])
      } yield new TarotApiServiceMock(usersRef, projectsRef, spreadsRef, cardsRef)
    }
}
