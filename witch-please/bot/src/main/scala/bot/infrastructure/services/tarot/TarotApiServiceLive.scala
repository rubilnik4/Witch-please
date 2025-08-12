package bot.infrastructure.services.tarot

import shared.api.dto.*
import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.authorize.*
import shared.api.dto.telegram.*
import shared.models.telegram.*
import sttp.client3.*
import sttp.client3.ziojson.asJsonEither
import sttp.model.{MediaType, Uri}
import zio.*
import zio.json.*
import shared.api.dto.tarot.projects.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
import shared.api.dto.tarot.common.*
import shared.api.dto.tarot.errors.TarotErrorResponse
import shared.infrastructure.services.clients.SttpClient

import scala.concurrent.duration.{DurationInt, FiniteDuration}

final class TarotApiServiceLive(baseUrl: String, client: SttpBackend[Task, Any]) extends TarotApiService:
  def createUser(request: UserCreateRequest): ZIO[Any, TarotErrorResponse, IdResponse] = {
    for {
      _ <- ZIO.logDebug(s"Sending create user request: ${request.name}; clientId: ${request.clientId}")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.userCreatePath(baseUrl))
      request = SttpClient.getPostRequest(uri)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, request)
    } yield response
  }
  
  def createProject(request: ProjectCreateRequest): ZIO[Any, TarotErrorResponse, IdResponse] = {
    for {
      _ <- ZIO.logDebug(s"Sending create project request: ${request.name}")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.projectCreatePath(baseUrl))
      request = SttpClient.getPostRequest(uri)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, request)
    } yield response
  }

  def createSpread(request: TelegramSpreadCreateRequest): ZIO[Any, TarotErrorResponse, IdResponse] = {
    for {
      _ <- ZIO.logDebug(s"Sending create spread request: ${request.title}; for project: ${request.projectId} ")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.spreadCreatePath(baseUrl))
      request = SttpClient.getPostRequest(uri)
        .response(asJsonEither[TarotErrorResponse, IdResponse])
      response <- SttpClient.sendJson(client, request)
    } yield response
  }

  def tokenAuth(request: AuthRequest): ZIO[Any, TarotErrorResponse, AuthResponse] = {
    for {
      _ <- ZIO.logDebug(s"Sending auth token request for user ${request.userId} and project ${request.projectId}")
      uri <- SttpClient.toSttpUri(TarotApiRoutes.tokenAuthPath(baseUrl))
      request = SttpClient.getPostRequest(uri)
        .response(asJsonEither[TarotErrorResponse, AuthResponse])
      response <- SttpClient.sendJson(client, request)
    } yield response
  }


