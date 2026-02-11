package bot.application.handlers.telegram.flows

import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.infrastructure.services.telegram.{TelegramApiService, TelegramPhotoResolver}
import zio.ZIO

import java.util.UUID

object PhotoFlow {
  def showPhoto(context: TelegramContext, photoId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Show photo $photoId command for chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))
      
      photo <- tarotApi.getPhoto(photoId, token)
      fileId <- TelegramPhotoResolver.getFileId(photo)
      _ <- telegramApi.sendPhoto(context.chatId, fileId)
    } yield ()
}
