package tarot.fixtures

import shared.models.tarot.authorize.ClientType
import shared.models.telegram.TelegramFile
import tarot.domain.models.*
import tarot.domain.models.authorize.*
import tarot.domain.models.photo.ExternalPhoto
import tarot.domain.models.projects.*
import tarot.domain.models.spreads.*
import tarot.infrastructure.services.PhotoServiceSpec
import tarot.layers.TarotEnv
import zio.ZIO

object TarotTestFixtures {
  def getPhoto: ZIO[TarotEnv, TarotError, String] =
    for {
      fileStorageService <- ZIO.serviceWith[TarotEnv](_.tarotService.fileStorageService)
      telegramApiService <- ZIO.serviceWith[TarotEnv](_.tarotService.telegramChannelService)
      photo <- fileStorageService.getResourcePhoto(PhotoServiceSpec.resourcePath)
        .mapError(error => TarotError.StorageError(error.getMessage, error.getCause))
      telegramFile = TelegramFile(photo.fileName, photo.bytes)
      chatId <- getChatId
      photoId <- telegramApiService.sendPhoto(chatId, telegramFile)
        .mapError(error => TarotErrorMapper.toTarotError("TelegramApiService", error))
    } yield photoId

  def getUser(clientId: String, clientType: ClientType, clientSecret: String): ZIO[TarotEnv, TarotError, UserId] =
    val user = ExternalUser(clientId, clientType, clientSecret, "test user")
    for {
      userHandler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.userCommandHandler)
      userId <- userHandler.createUser(user)
    } yield userId

  def getProject(userId: UserId): ZIO[TarotEnv, TarotError, ProjectId] =
    val project = ExternalProject("test project")
    for {
      projectHandler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.projectCommandHandler)
      projectId <- projectHandler.createProject(project, userId)
    } yield projectId

  def getToken(clientType: ClientType, clientSecret: String, userId: UserId, projectId: ProjectId)
  : ZIO[TarotEnv, TarotError, String] =
    for {
      authService <- ZIO.serviceWith[TarotEnv](_.tarotService.authService)
      token <- authService.issueToken(clientType, userId, clientSecret, Some(projectId))
    } yield token.token

  def getSpread(projectId: ProjectId, photoId: String): ZIO[TarotEnv, TarotError, SpreadId] =
    val photo = ExternalPhoto.Telegram(photoId)
    val spread = ExternalSpread(projectId, "Test spread", 1, photo)
    for {
      spreadHandler <- ZIO.serviceWith[TarotEnv](_.tarotCommandHandler.spreadCommandHandler)
      spreadId <- spreadHandler.createSpread(spread)
    } yield spreadId

  def getChatId: ZIO[TarotEnv, TarotError, Long] =
    for {
      telegramConfig <- ZIO.serviceWith[TarotEnv](_.config.telegram)
      chatId <- ZIO.fromOption(telegramConfig.chatId).orElseFail(TarotError.NotFound("chatId not set"))
    } yield chatId
}
