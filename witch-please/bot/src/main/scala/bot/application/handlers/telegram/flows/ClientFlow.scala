package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.ClientCommands
import bot.domain.models.telegram.TelegramContext
import bot.layers.BotEnv
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import zio.ZIO

object ClientFlow {
  def showAuthors(context: TelegramContext): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Get authors command for chat ${context.chatId}")

      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      
      authors <- tarotApi.getAuthors
      authorsButtons = authors.zipWithIndex.map { case (author, idx) =>
        TelegramInlineKeyboardButton(s"${idx + 1}. ${author.name}. ${author.spreadsCount} раскладов", 
          Some(ClientCommands.authorSelect(author.id)))
      }
      
      _ <- telegramApi.sendInlineButtons(context.chatId, "Выбери свою тарологичку", authorsButtons)
    } yield ()
}
