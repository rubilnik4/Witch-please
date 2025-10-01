package bot.application.handlers.telegram

import bot.domain.models.telegram.*
import bot.layers.BotEnv
import zio.ZIO

final class TelegramCommandHandlerLive extends TelegramCommandHandler {
  def handle(message: TelegramMessage): ZIO[BotEnv, Throwable, Unit] =
    message match {
      case TelegramMessage.Text(context, text) =>
        TelegramTextHandler.handle(context, text)
      case TelegramMessage.Photo(context, fileId) =>
        TelegramPhotoHandler.handle(context, fileId)
      case TelegramMessage.Command(context, cmd) =>
        TelegramRouterHandler.handle(context, cmd)
      case _ =>
        ZIO.logError("Unsupported telegram request type received")
    }    
}
