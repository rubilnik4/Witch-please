package bot.mocks

import bot.infrastructure.services.tarot.TarotApiService
import shared.api.dto.tarot.authorize.*
import shared.api.dto.tarot.cards.{CardCreateRequest, CardResponse, CardUpdateRequest}
import shared.api.dto.tarot.common.*
import shared.api.dto.tarot.photo.PhotoResponse
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
import shared.infrastructure.services.common.DateTimeService
import shared.models.api.ApiError
import shared.models.tarot.authorize.*
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
  spreadMap: Ref.Synchronized[Map[UUID, Map[UUID, SpreadResponse]]],
  cardMap: Ref.Synchronized[Map[UUID, Map[UUID, CardResponse]]]
) extends TarotApiService {

  override def createUser(request: UserCreateRequest): ZIO[Any, ApiError, IdResponse] =
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

  override def getUserByClientId(clientId: String): ZIO[Any, ApiError, UserResponse] =
    userMap.get.map(_.get(clientId)).flatMap {
      case Some(user) => ZIO.succeed(user)
      case None => ZIO.fail(ApiError.HttpCode(StatusCode.NotFound.code, s"user $clientId not found"))
    }

  override def getOrCreateUserId(request: UserCreateRequest): ZIO[Any, ApiError, UUID] =
    getUserByClientId(request.clientId).map(_.id).catchSome {
      case ApiError.HttpCode(code, _) if code == StatusCode.NotFound.code =>
        createUser(request).map(_.id)
    }

  override def getAuthors: ZIO[Any, ApiError, List[AuthorResponse]] =
    for {
      users <- userMap.get
      spreads <- spreadMap.get
    } yield {
      val spreadsCountByUserId: Map[UUID, Int] =
        spreads.map { case (userId, spreadsForUser) => userId -> spreadsForUser.size}

      val usersById: Map[UUID, UserResponse] =
       users.values.map(user => user.id -> user).toMap

      spreadsCountByUserId.toList.collect {
        case (userId, spreadsCount) if spreadsCount > 0 =>
          usersById.get(userId).map { user => AuthorResponse(userId, user.name, spreadsCount) }
      }.flatten
    }

  override def tokenAuth(request: AuthRequest): ZIO[Any, ApiError, AuthResponse] =
    val role = Role.Admin
    val tokenPayload = TokenPayload(
      clientType = request.clientType,
      userId = request.userId,
      role = role
    )
    val token = tokenPayload.toJson
    ZIO.succeed(AuthResponse(token = token, role = role))

  override def createSpread(request: SpreadCreateRequest, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      now <- DateTimeService.getDateTimeNow
      tokenPayload <- getTokenPayload(token)
      spreadId = UUID.randomUUID()
      spread = getSpreadResponse(spreadId, request, now)
      idResponse <- spreadMap.modifyZIO { spreads =>
        val userSpreads = spreads.getOrElse(tokenPayload.userId, Map.empty)
        val updatedProjectSpreads = userSpreads.updated(spreadId, spread)
        val updatedSpreads = spreads.updated(tokenPayload.userId, updatedProjectSpreads)
        ZIO.succeed(IdResponse(spreadId), updatedSpreads)
      }
    } yield idResponse

  override def getSpread(spreadId: UUID, token: String): ZIO[Any, ApiError, SpreadResponse] =
    spreadMap.get.flatMap { spreads =>
      val spread = spreads.valuesIterator.flatMap(_.get(spreadId)).nextOption()
      ZIO.fromOption(spread).orElseFail(ApiError.HttpCode(StatusCode.NotFound.code, s"Spread $spreadId not found"))
    }

  override def getSpreads(token: String): ZIO[Any, ApiError, List[SpreadResponse]] =
  for {
    tokenPayload <- getTokenPayload(token)
    spreads <- spreadMap.get.map(_.get(tokenPayload.userId)).flatMap {
      case Some(spreads) => ZIO.succeed(spreads.values.toList)
      case None => ZIO.succeed(List.empty)
    }
  } yield spreads

  override def getCard(cardId: UUID, token: String): ZIO[Any, ApiError, CardResponse] =
    cardMap.get.map(_.values.flatMap(_.get(cardId)).headOption).flatMap {
      case Some(card) => ZIO.succeed(card)
      case None => ZIO.fail(ApiError.HttpCode(StatusCode.NotFound.code, s"Card $cardId not found"))
    }

  override def getCards(spreadId: UUID, token: String): ZIO[Any, ApiError, List[CardResponse]] =
    cardMap.get.map(_.get(spreadId)).flatMap {
      case Some(cards) => ZIO.succeed(cards.values.toList)
      case None => ZIO.succeed(List.empty)
    }

  override def getCardsCount(spreadId: UUID, token: String): ZIO[Any, ApiError, Int] =
    cardMap.get.map(_.get(spreadId)).flatMap {
      case Some(cards) => ZIO.succeed(cards.size)
      case None => ZIO.succeed(0)
    }

  override def publishSpread(request: SpreadPublishRequest, spreadId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    ZIO.unit

  override def updateSpread(request: SpreadUpdateRequest, spreadId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    for {
      now <- DateTimeService.getDateTimeNow
      tokenPayload <- getTokenPayload(token)
      _ <- spreadMap.modifyZIO { spreads =>
        val userSpreads = spreads.getOrElse(tokenPayload.userId, Map.empty)
        userSpreads.get(spreadId) match {
          case None =>
            ZIO.fail(ApiError.HttpCode(StatusCode.NotFound.code, s"Spread $spreadId not found for user ${tokenPayload.userId}"))
          case Some(spread) =>
            val updatedSpread = getSpreadResponse(request, spread)
            val updatedUserSpreads = userSpreads.updated(spreadId, updatedSpread)
            val updatedSpreads = spreads.updated(tokenPayload.userId, updatedUserSpreads)
            ZIO.succeed((), updatedSpreads)
        }
      }
    } yield ()

  override def deleteSpread(spreadId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    for {
      tokenPayload <- getTokenPayload(token)
      _ <- spreadMap.modifyZIO { spreads =>
        val userSpreads = spreads.getOrElse(tokenPayload.userId, Map.empty)
        userSpreads.get(spreadId) match {
          case None =>
            ZIO.fail(ApiError.HttpCode(StatusCode.NotFound.code, s"Spread $spreadId not found for user ${tokenPayload.userId}"))
          case Some(spread) =>
            val deletedUserSpreads = userSpreads - spreadId
            val deletedSpreads =
              if (deletedUserSpreads.isEmpty) spreads - tokenPayload.userId
              else spreads.updated(tokenPayload.userId, deletedUserSpreads)
            ZIO.succeed((), deletedSpreads)
        }
      }
    } yield ()

  override def createCard(request: CardCreateRequest, spreadId: UUID, position: Int, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.fail(ApiError.HttpCode(StatusCode.BadRequest.code, "position must be positive")).when(position < 0)

      now <- DateTimeService.getDateTimeNow
      cardId = UUID.randomUUID()
      card = getCardResponse(cardId, request, position, spreadId, now)
      idResponse <- cardMap.modifyZIO { cards =>
        val spreadCards = cards.getOrElse(spreadId, Map.empty)
        val updatedSpreadCards = spreadCards.updated(cardId, card)
        val updatedCards = cards.updated(spreadId, updatedSpreadCards)
        ZIO.succeed(IdResponse(cardId), updatedCards)
      }
    } yield idResponse

  override def updateCard(request: CardUpdateRequest, cardId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    for {
      now <- DateTimeService.getDateTimeNow
      _ <- cardMap.modifyZIO { spreads =>
        val cardsMaybe =
          spreads.collectFirst {
            case (spreadId, cards) if cards.contains(cardId) =>
              val updated = getCardResponse(cards(cardId), request, now)
              val updatedCards = cards.updated(cardId, updated)
              val updatedSpreads = spreads.updated(spreadId, updatedCards)
              updatedSpreads
          }

        cardsMaybe match
          case Some(cards) =>
            ZIO.succeed((), cards)
          case None =>
            ZIO.fail(ApiError.HttpCode(StatusCode.NotFound.code, s"Card $cardId not found"))
        }
    } yield ()

  override def deleteCard(cardId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    cardMap.modifyZIO { spreads =>
      val cardsMaybe =
        spreads.collectFirst {
          case (spreadId, cards) if cards.contains(cardId) =>
            val deletedCards = cards - cardId
            val deletedSpreads =
              if (deletedCards.isEmpty) spreads - spreadId
              else spreads.updated(spreadId, deletedCards)
            deletedSpreads
        }
      cardsMaybe match
        case Some(cards) =>
          ZIO.succeed((), cards)
        case None =>
          ZIO.fail(ApiError.HttpCode(StatusCode.NotFound.code, s"Card $cardId not found"))
    }

  private def getUserResponse(id: UUID, request: UserCreateRequest, now: Instant) =
    UserResponse(
      id = id,
      clientId = request.clientId,
      clientType = ClientType.Telegram,
      name = request.name,
      createdAt = now
    )

  private def getSpreadResponse(id: UUID, request: SpreadCreateRequest, now: Instant) =
    SpreadResponse(
      id = id,
      title = request.title,
      cardsCount = request.cardCount,
      spreadStatus = SpreadStatus.Draft,
      photo = PhotoResponse(UUID.randomUUID(), UUID.randomUUID(), PhotoOwnerType.Spread, id, 
        request.photo.sourceType, request.photo.sourceId),
      createdAt =  now,
      scheduledAt = None,
      publishedAt = None
    )

  private def getSpreadResponse(request: SpreadUpdateRequest, spread: SpreadResponse) =
    SpreadResponse(
      id = spread.id,
      title = request.title,
      cardsCount = request.cardCount,
      spreadStatus = spread.spreadStatus,
      photo = PhotoResponse(spread.photo.id, spread.photo.fileId, PhotoOwnerType.Spread, spread.id, 
        request.photo.sourceType, request.photo.sourceId),
      createdAt = spread.createdAt,
      scheduledAt = spread.scheduledAt,
      publishedAt = spread.publishedAt
    )

  private def getCardResponse(id: UUID, request: CardCreateRequest, position: Int, spreadId: UUID, now: Instant) =
    CardResponse(
      id = id,
      position = position,
      spreadId = spreadId,
      title = request.title,
      photo = PhotoResponse(UUID.randomUUID(), UUID.randomUUID(), PhotoOwnerType.Card, id, 
        request.photo.sourceType, request.photo.sourceId),
      createdAt = now
    )

  private def getCardResponse(card: CardResponse, request: CardUpdateRequest, now: Instant) =
    CardResponse(
      id = card.id,
      position = card.position,
      spreadId = card.spreadId,
      title = request.title,
      photo = PhotoResponse(UUID.randomUUID(), UUID.randomUUID(), PhotoOwnerType.Card, card.id,
        request.photo.sourceType, request.photo.sourceId),
      createdAt = now
    )

  private def getTokenPayload(token: String): ZIO[Any, ApiError, TokenPayload] =
    ZIO.fromEither(token.fromJson[TokenPayload])
      .mapError(error => ApiError.InvalidResponse(token, error))
}

object TarotApiServiceMock {
  val tarotApiServiceLive: ULayer[TarotApiService] =
    ZLayer.fromZIO {
      for {
        usersRef <- Ref.Synchronized.make(Map.empty[String, UserResponse])
        spreadsRef <- Ref.Synchronized.make(Map.empty[UUID, Map[UUID, SpreadResponse]])
        cardsRef <- Ref.Synchronized.make(Map.empty[UUID, Map[UUID, CardResponse]])
      } yield new TarotApiServiceMock(usersRef, spreadsRef, cardsRef)
    }
}
