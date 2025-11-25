package bot.infrastructure.services.tarot

import shared.api.dto.*
import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.authorize.*
import shared.api.dto.tarot.cards.CardResponse
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

final class TarotApiServiceLive(baseUrl: String, client: SttpBackend[Task, Any]) extends TarotApiService:
  override def createUser(request: UserCreateRequest): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create user request: ${request.name}; clientId: ${request.clientId}")
      
      uri <- SttpClient.toSttpUri(TarotApiRoutes.authorCreatePath(baseUrl))
      userRequest = SttpClient.postRequest(uri, request)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, userRequest)
    } yield response

  override def getUserByClientId(clientId: String): ZIO[Any, ApiError, UserResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending get user request by clientId: $clientId")
      
      uri <- SttpClient.toSttpUri(TarotApiRoutes.userGetByClientIdPath(baseUrl, clientId))
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
      
      uri <- SttpClient.toSttpUri(TarotApiRoutes.authorsGetPath(baseUrl))
      authorsRequest = SttpClient.getRequest(uri)
        .response(asJsonEither[TarotErrorResponse, List[AuthorResponse]])
      response <- SttpClient.sendJson(client, authorsRequest)
    } yield response  

  override def createSpread(request: TelegramSpreadCreateRequest, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create spread request: ${request.title}")
      
      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadCreatePath(baseUrl))
      spreadRequest = SttpClient.postAuthRequest(uri, request, token)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, spreadRequest)
    } yield response

  override def getSpread(spreadId: UUID, token: String): ZIO[Any, ApiError, SpreadResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending get spread request by spreadId: $spreadId")
      
      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadGetPath(baseUrl, spreadId))
      spreadRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, SpreadResponse])
      response <- SttpClient.sendJson(client, spreadRequest)
    } yield response

  override def getSpreads(token: String): ZIO[Any, ApiError, List[SpreadResponse]] =
    for {
      _ <- ZIO.logDebug(s"Sending get spreads request")
      
      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadsGetPath(baseUrl))
      spreadsRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, List[SpreadResponse]])
      response <- SttpClient.sendJson(client, spreadsRequest)
    } yield response

  override def createCard(request: TelegramCardCreateRequest, spreadId: UUID, index: Int, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create card $index request: ${request.description}; for spread: $spreadId ")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.cardCreatePath(baseUrl, spreadId, index))
      cardRequest = SttpClient.postAuthRequest(uri, request, token)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, cardRequest)
    } yield response

  override def getCards(spreadId: UUID, token: String): ZIO[Any, ApiError, List[CardResponse]] =
    for {
      _ <- ZIO.logDebug(s"Sending get card request by spreadId: $spreadId")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.cardsGetPath(baseUrl, spreadId))
      cardsRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, List[CardResponse]])
      response <- SttpClient.sendJson(client, cardsRequest)
    } yield response

  override def getCardsCount(spreadId: UUID, token: String): ZIO[Any, ApiError, Int] =
    for {
      _ <- ZIO.logDebug(s"Sending get card count request by spreadId: $spreadId")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.cardsCountGetPath(baseUrl, spreadId))
      cardsRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, Int])
      response <- SttpClient.sendJson(client, cardsRequest)

    } yield response
  override def publishSpread(request: SpreadPublishRequest, spreadId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Sending publish request for spread $spreadId")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadPublishPath(baseUrl, spreadId))
      publishRequest = SttpClient.putAuthRequest(uri, request, token)
        .response(SttpClient.asJsonNoContent[TarotErrorResponse])
      _ <- SttpClient.sendNoContent(client, publishRequest)
    } yield()

  override def tokenAuth(request: AuthRequest): ZIO[Any, ApiError, AuthResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending auth token request for user ${request.userId}")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.tokenAuthPath(baseUrl))
      authRequest = SttpClient.postRequest(uri, request)
        .response(asJsonEither[TarotErrorResponse, AuthResponse])
      response <- SttpClient.sendJson(client, authRequest)
    } yield response