package bot.application.handlers.telegram.flows

import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.SessionRequire
import bot.layers.BotEnv
import shared.infrastructure.services.telegram.TelegramPhotoResolver
import shared.models.photo.PhotoSource
import zio.ZIO

import java.util.UUID

object PhotoFlow {
  def showPhoto(context: TelegramContext, photoId: UUID): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Show photo $photoId command for chat ${context.chatId}")

      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      token <- SessionRequire.token(context.chatId)
      
      photo <- tarotApi.getPhoto(photoId, token)
      fileId <- TelegramPhotoResolver.getFileId(PhotoSource(photo.sourceId, photo.sourceType, None))
      _ <- telegramApi.sendPhoto(context.chatId, fileId)
    } yield ()
}
