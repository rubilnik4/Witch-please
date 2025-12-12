package tarot.fixtures

import shared.models.files.FileSourceType
import shared.models.tarot.authorize.ClientType
import shared.models.telegram.TelegramFile
import tarot.application.commands.cards.commands.CreateCardCommand
import tarot.application.commands.spreads.commands.CreateSpreadCommand
import tarot.application.commands.users.commands.CreateAuthorCommand
import tarot.domain.models.*
import tarot.domain.models.authorize.*
import tarot.domain.models.cards.CardId
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.*
import tarot.layers.TarotEnv
import zio.ZIO

object TarotTestFixtures {
  final val resourcePath = "photos/test.png"
  
  def createPhoto: ZIO[TarotEnv, TarotError, String] =
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

  def createUser(clientId: String, clientType: ClientType, clientSecret: String): ZIO[TarotEnv, TarotError, UserId] =
    val author = getAuthorCommand(clientId, clientType, clientSecret)
    for {
      userHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.userCommandHandler)
      userId <- userHandler.createAuthor(author)
    } yield userId

  def createToken(clientType: ClientType, clientSecret: String, userId: UserId): ZIO[TarotEnv, TarotError, String] =
    for {
      authService <- ZIO.serviceWith[TarotEnv](_.services.authService)
      token <- authService.issueToken(clientType, userId, clientSecret)
    } yield token.token

  def createSpread(userId: UserId, cardsCount: Int, photoId: String): ZIO[TarotEnv, TarotError, SpreadId] =
    val photo = PhotoSource(FileSourceType.Telegram, photoId)
    val command = getSpreadCommand(userId, cardsCount, photo)
    for {
      spreadHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.spreadCommandHandler)
      spreadId <- spreadHandler.createSpread(command)
    } yield spreadId

  def createCards(spreadId: SpreadId, cardCount: Int, photoId: String): ZIO[TarotEnv, TarotError, List[CardId]] =
    val photo = PhotoSource(FileSourceType.Telegram, photoId)
    for {
      cardHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.cardCommandHandler)
      cardIds <- ZIO.foreach(0 until cardCount) { position =>
        val command = CreateCardCommand(position, spreadId, "Test card", photo)
        cardHandler.createCard(command)
      }
    } yield cardIds.toList

  def getChatId: ZIO[TarotEnv, TarotError, Long] =
    for {
      telegramConfig <- ZIO.serviceWith[TarotEnv](_.config.telegram)
      chatId <- ZIO.fromOption(telegramConfig.chatId).orElseFail(TarotError.NotFound("chatId not set"))
    } yield chatId
    
  private def getAuthorCommand(clientId: String, clientType: ClientType, clientSecret: String) =
    CreateAuthorCommand(
      clientId = clientId,
      clientType = clientType,
      clientSecret = clientSecret,
      name = "test user")
  
  private def getSpreadCommand(userId: UserId, cardsCount: Int, photo: PhotoSource) =
    CreateSpreadCommand(
      userId = userId,
      title = "Test spread",
      cardsCount = cardsCount,
      description = "Test spread",
      photo = photo)
}
