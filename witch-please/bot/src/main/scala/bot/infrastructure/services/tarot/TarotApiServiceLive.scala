package bot.infrastructure.services.tarot

import shared.api.dto.*
import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.authorize.*
import shared.api.dto.tarot.cards.{CardCreateRequest, CardResponse, CardUpdateRequest}
import shared.api.dto.tarot.common.*
import shared.api.dto.tarot.errors.TarotErrorResponse
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
import shared.infrastructure.services.clients.SttpClient
import shared.models.api.*
import sttp.client3.*
import sttp.client3.ziojson.asJsonEither
import sttp.model.{StatusCode, Uri}
import zio.*
import zio.json.*

import java.util.UUID

final class TarotApiServiceLive(apiUrl: TarotApiUrl, client: SttpBackend[Task, Any]) extends TarotApiService {
  override def createUser(request: UserCreateRequest): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create user request: ${request.name}; clientId: ${request.clientId}")
      
      uri <- SttpClient.toSttpUri(TarotApiRoutes.authorCreatePath(apiUrl.url))
      userRequest = SttpClient.postRequest(uri, request)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, userRequest)
    } yield response

  override def getUserByClientId(clientId: String): ZIO[Any, ApiError, UserResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending get user request by clientId: $clientId")
      
      uri <- SttpClient.toSttpUri(TarotApiRoutes.userGetByClientIdPath(apiUrl.url, clientId))
      userRequest = SttpClient.getRequest(uri)
        .response(asJsonEither[TarotErrorResponse, UserResponse])
      response <- SttpClient.sendJson(client, userRequest)
    } yield response

  override def getOrCreateUserId(request: UserCreateRequest): ZIO[Any, ApiError, UUID] =
    getUserByClientId(request.clientId).map(_.id)
      .catchSome {
        case ApiError.HttpCode(StatusCode.NotFound.code, _) =>
          for {
            _ <- ZIO.logDebug(s"User not found, creating new one: ${request.clientId}")
            idResponse <- createUser(request)
          } yield idResponse.id
    }

  override def getAuthors: ZIO[Any, ApiError, List[AuthorResponse]] =
    for {
      _ <- ZIO.logDebug(s"Sending get authors request")
      
      uri <- SttpClient.toSttpUri(TarotApiRoutes.authorsGetPath(apiUrl.url))
      authorsRequest = SttpClient.getRequest(uri)
        .response(asJsonEither[TarotErrorResponse, List[AuthorResponse]])
      response <- SttpClient.sendJson(client, authorsRequest)
    } yield response

  override def tokenAuth(request: AuthRequest): ZIO[Any, ApiError, AuthResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending auth token request for user ${request.userId}")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.tokenAuthPath(apiUrl.url))
      authRequest = SttpClient.postRequest(uri, request)
        .response(asJsonEither[TarotErrorResponse, AuthResponse])
      response <- SttpClient.sendJson(client, authRequest)
    } yield response

  override def getSpread(spreadId: UUID, token: String): ZIO[Any, ApiError, SpreadResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending get spread request by spreadId: $spreadId")

      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadGetPath(apiUrl.url, spreadId))
      spreadRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, SpreadResponse])
      response <- SttpClient.sendJson(client, spreadRequest)
    } yield response

  override def getSpreads(token: String): ZIO[Any, ApiError, List[SpreadResponse]] =
    for {
      _ <- ZIO.logDebug(s"Sending get spreads request")

      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadsGetPath(apiUrl.url))
      spreadsRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, List[SpreadResponse]])
      response <- SttpClient.sendJson(client, spreadsRequest)
    } yield response
    
  override def createSpread(request: SpreadCreateRequest, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create spread request: ${request.title}")
      
      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadCreatePath(apiUrl.url))
      spreadRequest = SttpClient.postAuthRequest(uri, request, token)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, spreadRequest)
    } yield response

  override def updateSpread(request: SpreadUpdateRequest, spreadId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Sending update spread $spreadId request")

      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadUpdatePath(apiUrl.url, spreadId))
      spreadRequest = SttpClient.putAuthRequest(uri, request, token)
        .response(SttpClient.asJsonNoContent[TarotErrorResponse])
      _ <- SttpClient.sendNoContent(client, spreadRequest)
    } yield ()

  override def deleteSpread(spreadId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Sending delete spread $spreadId request")

      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadDeletePath(apiUrl.url, spreadId))
      spreadRequest = SttpClient.deleteAuthRequest(uri, token)
        .response(SttpClient.asJsonNoContent[TarotErrorResponse])
      _ <- SttpClient.sendNoContent(client, spreadRequest)
    } yield ()
    
  override def publishSpread(request: SpreadPublishRequest, spreadId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Sending publish request for spread $spreadId")

      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadPublishPath(apiUrl.url, spreadId))
      publishRequest = SttpClient.putAuthRequest(uri, request, token)
        .response(SttpClient.asJsonNoContent[TarotErrorResponse])
      _ <- SttpClient.sendNoContent(client, publishRequest)
    } yield()

  override def createCard(request: CardCreateRequest, spreadId: UUID, position: Int, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create card $position request: ${request.title}; for spread: $spreadId")

      uri <- SttpClient.toSttpUri(TarotApiRoutes.cardCreatePath(apiUrl.url, spreadId))
      cardRequest = SttpClient.postAuthRequest(uri, request, token)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, cardRequest)
    } yield response

  override def updateCard(request: CardUpdateRequest, cardId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Sending update card $cardId request")

      uri <- SttpClient.toSttpUri(TarotApiRoutes.cardUpdatePath(apiUrl.url, cardId))
      cardRequest = SttpClient.putAuthRequest(uri, request, token)
        .response(SttpClient.asJsonNoContent[TarotErrorResponse])
      response <- SttpClient.sendJson(client, cardRequest)
    } yield response

  override def deleteCard(cardId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Sending delete card $cardId request")

      uri <- SttpClient.toSttpUri(TarotApiRoutes.cardDeletePath(apiUrl.url, cardId))
      cardRequest = SttpClient.deleteAuthRequest(uri, token)
        .response(SttpClient.asJsonNoContent[TarotErrorResponse])
      response <- SttpClient.sendJson(client, cardRequest)
    } yield response

  override def getCard(cardId: UUID, token: String): ZIO[Any, ApiError, CardResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending get card request by cardId: $cardId")

      uri <- SttpClient.toSttpUri(TarotApiRoutes.cardGetPath(apiUrl.url, cardId))
      cardRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, CardResponse])
      response <- SttpClient.sendJson(client, cardRequest)
    } yield response

  override def getCards(spreadId: UUID, token: String): ZIO[Any, ApiError, List[CardResponse]] =
    for {
      _ <- ZIO.logDebug(s"Sending get cards request by spreadId: $spreadId")

      uri <- SttpClient.toSttpUri(TarotApiRoutes.cardsGetPath(apiUrl.url, spreadId))
      cardsRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, List[CardResponse]])
      response <- SttpClient.sendJson(client, cardsRequest)
    } yield response

  override def getCardsCount(spreadId: UUID, token: String): ZIO[Any, ApiError, Int] =
    for {
      _ <- ZIO.logDebug(s"Sending get card count request by spreadId: $spreadId")

      uri <- SttpClient.toSttpUri(TarotApiRoutes.cardsCountGetPath(apiUrl.url, spreadId))
      cardsRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, Int])
      response <- SttpClient.sendJson(client, cardsRequest)
    } yield response
}