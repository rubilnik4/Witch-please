package tarot.integration

import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import tarot.api.dto.common.IdResponse
import tarot.api.dto.tarot.spreads.*
import tarot.api.endpoints.*
import tarot.application.commands.projects.ProjectCreateCommand
import tarot.application.commands.users.UserCreateCommand
import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{ClientType, ExternalUser, UserId}
import tarot.domain.models.contracts.TarotChannelType
import tarot.domain.models.projects.{ExternalProject, ProjectId}
import tarot.domain.models.spreads.{SpreadId, SpreadStatus}
import tarot.infrastructure.services.PhotoServiceSpec.resourcePath
import tarot.infrastructure.services.clients.ZIOHttpClient
import tarot.layers.TestAppEnvLayer.testAppEnvLive
import tarot.layers.AppEnv
import tarot.models.TestSpreadState
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.TestAspect.sequential

import java.util.UUID

object SpreadIntegrationSpec extends ZIOSpecDefault {
  private val cardCount = 2
  private val clientId = "123456789"
  private val clientType = ClientType.Telegram
  private val clientSecret = "test-secret-token"

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

        app = ZioHttpInterpreter().toHttp(List(SpreadEndpoint.postSpreadEndpoint))
        spreadRequest = spreadCreateRequest(projectId, cardCount, photoId)
        request = ZIOHttpClient.getAuthPostRequest(URL(spreadCreatePath), spreadRequest, token)
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

        app = ZioHttpInterpreter().toHttp(List(SpreadEndpoint.postCardEndpoint))
        cardIds <- ZIO.foreach(0 until cardCount) { index =>
          val cardRequest = cardCreateRequest(photoId)
          val request = ZIOHttpClient.getAuthPostRequest(URL(cardCreatePath(spreadId, index)), cardRequest, token)
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

        app = ZioHttpInterpreter().toHttp(List(SpreadEndpoint.publishSpreadEndpoint))
        publishRequest <- spreadPublishRequest
        request = ZIOHttpClient.getAuthPutRequest(URL(spreadPublishPath(spreadId)), publishRequest, token)
        _ <- app.runZIO(request)

        spreadRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository.spreadRepository)
        spread <- spreadRepository.getSpread(SpreadId(spreadId))
      } yield assertTrue(
        spread.isDefined,
        spread.exists(_.spreadStatus == SpreadStatus.Ready),
        spread.exists(_.scheduledAt.contains(publishRequest.scheduledAt))
      )
    }
  ).provideShared(
    Scope.default,
    testAppEnvLive,
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

  private def spreadPublishRequest: ZIO[AppEnv, Nothing, SpreadPublishRequest] =
    for {
      now <- Clock.instant
      minFutureTime <- ZIO.serviceWith[AppEnv](_.appConfig.project.minFutureTime)
      publishTime = minFutureTime.plus(10.minute)
    } yield SpreadPublishRequest(
      scheduledAt = now.plus(publishTime)
    )

  private def getPhoto: ZIO[AppEnv, TarotError, String] =
    for {
      fileStorageService <- ZIO.serviceWith[AppEnv](_.tarotService.fileStorageService)
      telegramService <- ZIO.serviceWith[AppEnv](_.tarotService.telegramFileService)
      telegramConfig <- ZIO.serviceWith[AppEnv](_.appConfig.telegram)
      photo <- fileStorageService.getResourcePhoto(resourcePath)
      photoId <- telegramService.sendPhoto(telegramConfig.chatId, photo)
    } yield photoId

  private def getUser(clientId: String, clientType: ClientType, clientSecret: String): ZIO[AppEnv, TarotError, UserId] =
    val user = ExternalUser(clientId, clientType, clientSecret, "test user")
    val userCommand = UserCreateCommand(user)
    for {
      userHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.userCreateCommandHandler)
      userId <- userHandler.handle(userCommand)
    } yield userId

  private def getProject(userId: UserId): ZIO[AppEnv, TarotError, ProjectId] =
    val project = ExternalProject("test project")
    val projectCommand = ProjectCreateCommand(project, userId)
    for {
      projectHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.projectCreateCommandHandler)
      projectId <- projectHandler.handle(projectCommand)
    } yield projectId

  private def getToken(clientType: ClientType, clientSecret: String, userId: UserId, projectId: ProjectId)
      : ZIO[AppEnv, TarotError, String] =
    for {
      authService <- ZIO.serviceWith[AppEnv](_.tarotService.authService)
      token <- authService.issueToken(clientType, userId, clientSecret, Some(projectId))
    } yield token.token

  private def spreadCreatePath =
    Path.root / ApiPath.apiPath / TarotChannelType.Telegram / "spread"

  private def spreadPublishPath(spreadId: UUID) =
    Path.root / ApiPath.apiPath / "spread" / spreadId.toString / "publish"

  private def cardCreatePath(spreadId: UUID, index: Int) = {
    Path.root / ApiPath.apiPath / TarotChannelType.Telegram / "spread" / spreadId.toString / "cards" / index.toString
  }
}
