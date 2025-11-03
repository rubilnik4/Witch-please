package bot.infrastructure.services.tarot

import shared.api.dto.*
import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.authorize.*
import shared.api.dto.tarot.cards.CardResponse
import shared.api.dto.tarot.common.*
import shared.api.dto.tarot.errors.TarotErrorResponse
import shared.api.dto.tarot.projects.*
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
  def createUser(request: UserCreateRequest): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create user request: ${request.name}; clientId: ${request.clientId}")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.userCreatePath(baseUrl))
      userRequest = SttpClient.postRequest(uri, request)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, userRequest)
    } yield response

  def getUserByClientId(clientId: String): ZIO[Any, ApiError, UserResponse] = 
    for {
      _ <- ZIO.logDebug(s"Sending get user request by clientId: $clientId")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.userGetByClientIdPath(baseUrl, clientId))
      userRequest = SttpClient.getRequest(uri)
        .response(asJsonEither[TarotErrorResponse, UserResponse])
      response <- SttpClient.sendJson(client, userRequest)
    } yield response

  def getOrCreateUserId(request: UserCreateRequest): ZIO[Any, ApiError, UUID] =
    getUserByClientId(request.clientId).map(_.id)
      .catchSome {
        case ApiError.HttpCode(StatusCode.NotFound.code, _) =>
          for {
            _ <- ZIO.logDebug(s"User not found, creating new one: ${request.clientId}")
            idResponse <- createUser(request)
          } yield idResponse.id
    }

  def createProject(request: ProjectCreateRequest, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create project request: ${request.name}")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.projectCreatePath(baseUrl))
      projectRequest = SttpClient.postAuthRequest(uri, request, token)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, projectRequest)
    } yield response

  def getProjects(userId: UUID, token: String): ZIO[Any, ApiError, List[ProjectResponse]] =
    for {
      _ <- ZIO.logDebug(s"Sending get projects request by userId: $userId")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.projectsGetPath(baseUrl, userId))
      projectsRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, List[ProjectResponse]])
      response <- SttpClient.sendJson(client, projectsRequest)
    } yield response

  def createSpread(request: TelegramSpreadCreateRequest, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create spread request: ${request.title}; for project: ${request.projectId} ")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadCreatePath(baseUrl))
      spreadRequest = SttpClient.postAuthRequest(uri, request, token)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, spreadRequest)
    } yield response
    
  def getSpread(spreadId: UUID, token: String): ZIO[Any, ApiError, SpreadResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending get spread request by spreadId: $spreadId")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadGetPath(baseUrl, spreadId))
      spreadRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, SpreadResponse])
      response <- SttpClient.sendJson(client, spreadRequest)
    } yield response
    
  def getSpreads(projectId: UUID, token: String): ZIO[Any, ApiError, List[SpreadResponse]] =
    for {
      _ <- ZIO.logDebug(s"Sending get spreads request by projectId: $projectId")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadsGetPath(baseUrl, projectId))
      spreadsRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, List[SpreadResponse]])
      response <- SttpClient.sendJson(client, spreadsRequest)
    } yield response

  def createCard(request: TelegramCardCreateRequest, spreadId: UUID, index: Int, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create card $index request: ${request.description}; for spread: $spreadId ")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.cardCreatePath(baseUrl, spreadId, index))
      cardRequest = SttpClient.postAuthRequest(uri, request, token)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, cardRequest)
    } yield response

  def getCards(spreadId: UUID, token: String): ZIO[Any, ApiError, List[CardResponse]] =
    for {
      _ <- ZIO.logDebug(s"Sending get card request by spreadId: $spreadId")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.cardsGetPath(baseUrl, spreadId))
      cardsRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, List[CardResponse]])
      response <- SttpClient.sendJson(client, cardsRequest)
    } yield response

  def getCardsCount(spreadId: UUID, token: String): ZIO[Any, ApiError, Int] =
    for {
      _ <- ZIO.logDebug(s"Sending get card count request by spreadId: $spreadId")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.cardsCountGetPath(baseUrl, spreadId))
      cardsRequest = SttpClient.getAuthRequest(uri, token)
        .response(asJsonEither[TarotErrorResponse, Int])
      response <- SttpClient.sendJson(client, cardsRequest)

    } yield response
  def publishSpread(request: SpreadPublishRequest, spreadId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Sending publish request for spread $spreadId")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadPublishPath(baseUrl, spreadId))
      publishRequest = SttpClient.putAuthRequest(uri, request, token)
        .response(SttpClient.asJsonNoContent[TarotErrorResponse])
      _ <- SttpClient.sendNoContent(client, publishRequest)
    } yield()

  def tokenAuth(request: AuthRequest): ZIO[Any, ApiError, AuthResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending auth token request for user ${request.userId} and project ${request.projectId}")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.tokenAuthPath(baseUrl))
      authRequest = SttpClient.postRequest(uri, request)
        .response(asJsonEither[TarotErrorResponse, AuthResponse])
      response <- SttpClient.sendJson(client, authRequest)
    } yield response

