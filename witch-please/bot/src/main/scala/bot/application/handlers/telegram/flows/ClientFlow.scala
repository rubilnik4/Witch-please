package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.TelegramCommands
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
import zio.ZIO

object ClientFlow {
  def showAuthors(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Get authors command for chat ${context.chatId}")

      authors <- tarotApi.getAuthors
      authorsButtons = authors.zipWithIndex.map { case (author, idx) =>
        TelegramInlineKeyboardButton(s"${idx + 1}. ${author.name}. ${author.spreadsCount} раскладов", 
          Some(TelegramCommands.clientAuthorSelect(author.id)))
      }
      
      _ <- telegramApi.sendInlineButtons(context.chatId, "Выбери свою тарологичку", authorsButtons)
    } yield ()
}
