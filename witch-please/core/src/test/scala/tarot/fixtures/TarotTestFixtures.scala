package tarot.fixtures

import shared.models.files.FileSourceType
import shared.models.tarot.authorize.ClientType
import shared.models.telegram.TelegramFile
import tarot.application.commands.spreads.commands.CreateSpreadCommand
import tarot.application.commands.users.commands.CreateAuthorCommand
import tarot.domain.models.*
import tarot.domain.models.authorize.*
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.*
import tarot.layers.TarotEnv
import zio.ZIO

object TarotTestFixtures {
  final val resourcePath = "photos/test.png"
  
  def getPhoto: ZIO[TarotEnv, TarotError, String] =
    for {
      fileStorageService <- ZIO.serviceWith[TarotEnv](_.services.fileStorageService)
      telegramApiService <- ZIO.serviceWith[TarotEnv](_.services.telegramChannelService)
      photo <- fileStorageService.getResourceFile(resourcePath)
        .mapError(error => TarotError.StorageError(error.getMessage, error.getCause))
      telegramFile = TelegramFile(photo.fileName, photo.bytes)
      chatId <- getChatId
      photoId <- telegramApiService.sendPhoto(chatId, telegramFile)
        .mapError(error => TarotErrorMapper.toTarotError("TelegramApiService", error))
    } yield photoId

  def getUser(clientId: String, clientType: ClientType, clientSecret: String): ZIO[TarotEnv, TarotError, UserId] =
    val user = CreateAuthorCommand(clientId, clientType, clientSecret, "test user")
    for {
      userHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.userCommandHandler)
      userId <- userHandler.createAuthor(user)
    } yield userId

  def getToken(clientType: ClientType, clientSecret: String, userId: UserId)
  : ZIO[TarotEnv, TarotError, String] =
    for {
      authService <- ZIO.serviceWith[TarotEnv](_.services.authService)
      token <- authService.issueToken(clientType, userId, clientSecret)
    } yield token.token

  def getSpread(userId: UserId, photoId: String): ZIO[TarotEnv, TarotError, SpreadId] =
    val photo = PhotoSource(FileSourceType.Telegram ,photoId)
    val command = CreateSpreadCommand(userId, "Test spread", 1, photo)
    for {
      spreadHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.spreadCommandHandler)
      spreadId <- spreadHandler.createSpread(command)
    } yield spreadId

  def getChatId: ZIO[TarotEnv, TarotError, Long] =
    for {
      telegramConfig <- ZIO.serviceWith[TarotEnv](_.config.telegram)
      chatId <- ZIO.fromOption(telegramConfig.chatId).orElseFail(TarotError.NotFound("chatId not set"))
    } yield chatId
}
