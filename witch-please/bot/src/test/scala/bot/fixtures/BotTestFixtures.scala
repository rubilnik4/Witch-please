package bot.fixtures

import bot.layers.BotEnv
import shared.models.telegram.TelegramFile
import zio.ZIO

object BotTestFixtures {
  final val resourcePath = "photos/test.png"
  
  def getPhoto: ZIO[BotEnv, Throwable, String] =
    for {
      resourceFileService <- ZIO.serviceWith[BotEnv](_.services.resourceFileService)
      telegramApiService <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      photo <- resourceFileService.getResourceFile(resourcePath)
      telegramFile = TelegramFile(photo.fileName, photo.bytes)
      chatId <- getChatId
      photoId <- telegramApiService.sendPhoto(chatId, telegramFile)
    } yield photoId

  def getChatId: ZIO[BotEnv, Throwable, Long] =
    for {
      telegramConfig <- ZIO.serviceWith[BotEnv](_.config.telegram)
      chatId <- ZIO.fromOption(telegramConfig.chatId).orElseFail(RuntimeException("chatId not set"))
    } yield chatId
}
