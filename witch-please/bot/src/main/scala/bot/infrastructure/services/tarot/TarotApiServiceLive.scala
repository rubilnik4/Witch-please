package bot.infrastructure.services.tarot

import shared.api.dto.*
import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.authorize.*
import shared.api.dto.telegram.*
import shared.models.telegram.*
import sttp.client3.*
import sttp.client3.ziojson.{asJson, asJsonEither}
import sttp.model.{MediaType, Uri}
import zio.*
import zio.json.*
import shared.api.dto.tarot.projects.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
import shared.api.dto.tarot.common.*
import shared.api.dto.tarot.errors.TarotErrorResponse
import shared.infrastructure.services.clients.SttpClient
import shared.models.api.*

import java.util.UUID
import scala.concurrent.duration.{DurationInt, FiniteDuration}

final class TarotApiServiceLive(baseUrl: String, client: SttpBackend[Task, Any]) extends TarotApiService:
  def createUser(request: UserCreateRequest): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create user request: ${request.name}; clientId: ${request.clientId}")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.userCreatePath(baseUrl))
      userRequest = SttpClient.getPostRequest(uri, request)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, userRequest)
    } yield response
  
  def createProject(request: ProjectCreateRequest, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create project request: ${request.name}")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.projectCreatePath(baseUrl))
      projectRequest = SttpClient.getPostRequestAuth(uri, request, token)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, projectRequest)
    } yield response

  def createSpread(request: TelegramSpreadCreateRequest, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create spread request: ${request.title}; for project: ${request.projectId} ")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadCreatePath(baseUrl))
      spreadRequest = SttpClient.getPostRequestAuth(uri, request, token)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, spreadRequest)
    } yield response

  def createCard(request: TelegramCardCreateRequest, spreadId: UUID, index: Int, token: String): ZIO[Any, ApiError, IdResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending create card $index request: ${request.description}; for spread: $spreadId ")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.cardCreatePath(baseUrl, spreadId, index))
      cardRequest = SttpClient.getPostRequestAuth(uri, request, token)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, cardRequest)
    } yield response

  def publishSpread(request: SpreadPublishRequest, spreadId: UUID, token: String): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Sending publish request for spread $spreadId")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadPublishPath(baseUrl, spreadId))
      publishRequest = SttpClient.getPutRequest(uri, request)
        .response(SttpClient.asJsonNoContent[TarotErrorResponse])
      _ <- SttpClient.sendNoContent(client, publishRequest)
    } yield()

  def tokenAuth(request: AuthRequest): ZIO[Any, ApiError, AuthResponse] =
    for {
      _ <- ZIO.logDebug(s"Sending auth token request for user ${request.userId} and project ${request.projectId}")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.tokenAuthPath(baseUrl))
      authRequest = SttpClient.getPostRequest(uri, request)
        .response(asJsonEither[TarotErrorResponse, AuthResponse])
      response <- SttpClient.sendJson(client, authRequest)
    } yield response

