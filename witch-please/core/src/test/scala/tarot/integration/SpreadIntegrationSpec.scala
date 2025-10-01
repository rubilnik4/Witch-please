package tarot.integration

import shared.api.dto.tarot.TarotApiRoutes
import shared.api.dto.tarot.common.IdResponse
import shared.api.dto.tarot.spreads.*
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.models.tarot.authorize.ClientType
import shared.models.telegram.TelegramFile
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import tarot.api.endpoints.*
import tarot.application.commands.projects.ProjectCreateCommand
import tarot.application.commands.users.UserCreateCommand
import tarot.domain.models.authorize.*
import tarot.domain.models.projects.*
import tarot.domain.models.spreads.*
import tarot.domain.models.{TarotError, TarotErrorMapper}
import tarot.infrastructure.services.PhotoServiceSpec.resourcePath
import tarot.layers.{TarotEnv, TestTarotEnvLayer}
import tarot.layers.TestTarotEnvLayer
import tarot.models.TestSpreadState
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

import java.util.UUID

object SpreadIntegrationSpec extends ZIOSpecDefault {
  private final val cardCount = 2
  private final val clientId = "123456789"
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Spread API integration")(
    test("initialize test state") {
      for {
        photoId <- getPhoto
        userId <- getUser(clientId, clientType, clientSecret)
        projectId <- getProject(userId)
        token <- getToken(clientType, clientSecret, userId, projectId)

        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        _ <- ref.set(TestSpreadState(Some(photoId), Some(projectId.id), Some(token), None))
      } yield assertTrue(photoId.nonEmpty)
    },

    test("should send photo, get fileId, and create spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))
        projectId <- ZIO.fromOption(state.projectId).orElseFail(TarotError.NotFound("projectId not set"))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        spreadRequest = spreadCreateRequest(projectId, cardCount, photoId)
        request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.spreadCreatePath(""), spreadRequest, token)
        response <- app.runZIO(request)
        spreadId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)

        _ <- ref.set(TestSpreadState(Some(photoId), Some(projectId), Some(token), Some(spreadId)))
      } yield assertTrue(spreadId.toString.nonEmpty)
    },

    test("should send card to current spread") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestSpreadState]]
        state <- ref.get
        photoId <- ZIO.fromOption(state.photoId).orElseFail(TarotError.NotFound("photoId not set"))
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        cardIds <- ZIO.foreach(0 until cardCount) { index =>
          val cardRequest = cardCreateRequest(photoId)
          val request = ZIOHttpClient.postAuthRequest(TarotApiRoutes.cardCreatePath("", spreadId, index), cardRequest, token)
          for {
            response <- app.runZIO(request)
            cardId <- ZIOHttpClient.getResponse[IdResponse](response).map(_.id)
          } yield cardId
        }
      } yield assertTrue(cardIds.forall(id => id.toString.nonEmpty))
    },

    test("should publish spread") {
      for {
        _ <- TestClock.adjust(10.minute)
        state <- ZIO.serviceWithZIO[Ref.Synchronized[TestSpreadState]](_.get)
        spreadId <- ZIO.fromOption(state.spreadId).orElseFail(TarotError.NotFound("spreadId not set"))
        token <- ZIO.fromOption(state.token).orElseFail(TarotError.NotFound("token not set"))

        app = ZioHttpInterpreter().toHttp(SpreadEndpoint.endpoints)
        publishRequest <- spreadPublishRequest
        request = ZIOHttpClient.putAuthRequest(TarotApiRoutes.spreadPublishPath("", spreadId), publishRequest, token)
        _ <- app.runZIO(request)

        spreadRepository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.spreadRepository)
        spread <- spreadRepository.getSpread(SpreadId(spreadId))
      } yield assertTrue(
        spread.isDefined,
        spread.exists(_.spreadStatus == SpreadStatus.Ready),
        spread.exists(_.scheduledAt.contains(publishRequest.scheduledAt))
      )
    }
  ).provideShared(
    Scope.default,
    TestTarotEnvLayer.testEnvLive,
    testSpreadStateLayer
  ) @@ sequential

  private val testSpreadStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestSpreadState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestSpreadState(None, None, None, None)))
    
  private def spreadCreateRequest(projectId: UUID, cardCount: Int, photoId: String) =
    TelegramSpreadCreateRequest(
      projectId = projectId,
      title = "Spread integration test",
      cardCount = cardCount,
      coverPhotoId = photoId
    )

  private def cardCreateRequest(photoId: String) =
    TelegramCardCreateRequest(
      description = "Card integration test",
      coverPhotoId = photoId
    )

  private def spreadPublishRequest: ZIO[TarotEnv, Nothing, SpreadPublishRequest] =
    for {
      now <- Clock.instant
      minFutureTime <- ZIO.serviceWith[TarotEnv](_.config.project.minFutureTime)
      publishTime = minFutureTime.plus(10.minute)
    } yield SpreadPublishRequest(
      scheduledAt = now.plus(publishTime)
    )

  private def getPhoto: ZIO[TarotEnv, TarotError, String] =
    for {
      fileStorageService <- ZIO.serviceWith[TarotEnv](_.tarotService.fileStorageService)
      telegramApiService <- ZIO.serviceWith[TarotEnv](_.tarotService.telegramApiService)
      photo <- fileStorageService.getResourcePhoto(resourcePath)
        .mapError(error => TarotError.StorageError(error.getMessage, error.getCause))
      telegramFile = TelegramFile(photo.fileName, photo.bytes)
      chatId <- getChatId
      photoId <- telegramApiService.sendPhoto(chatId, telegramFile)
        .mapError(error => TarotErrorMapper.toTarotError("TelegramApiService", error))
    } yield photoId

  private def getUser(clientId: String, clientType: ClientType, clientSecret: String): ZIO[TarotEnv, TarotError, UserId] =
    val user = ExternalUser(clientId, clientType, clientSecret, "test user")
    val userCommand = UserCreateCommand(user)
    for {
      userHandler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.userCreateCommandHandler)
      userId <- userHandler.handle(userCommand)
    } yield userId

  private def getProject(userId: UserId): ZIO[TarotEnv, TarotError, ProjectId] =
    val project = ExternalProject("test project")
    val projectCommand = ProjectCreateCommand(project, userId)
    for {
      projectHandler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.projectCreateCommandHandler)
      projectId <- projectHandler.handle(projectCommand)
    } yield projectId

  private def getToken(clientType: ClientType, clientSecret: String, userId: UserId, projectId: ProjectId)
      : ZIO[TarotEnv, TarotError, String] =
    for {
      authService <- ZIO.serviceWith[TarotEnv](_.tarotService.authService)
      token <- authService.issueToken(clientType, userId, clientSecret, Some(projectId))
    } yield token.token

  private def getChatId: ZIO[TarotEnv, TarotError, Long] =
    for {
      telegramConfig <- ZIO.serviceWith[TarotEnv](_.config.telegram)
      chatId <- ZIO.fromOption(telegramConfig.chatId).orElseFail(TarotError.NotFound("chatId not set"))
    } yield chatId  
}
