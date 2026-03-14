package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.AuthorCommands
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
import zio.ZIO

object CommonFlow {
  def sendEditReply(context: TelegramContext, buttonText: String, currentValue: Option[String]): ZIO[BotEnv, Throwable, Unit] = {
    val currentBlock = currentValue.fold("")(value => s"\nТекущее: $value\n")
    val text = s"""$buttonText$currentBlock""".trim
    val keepButton = TelegramInlineKeyboardButton("Оставить текущее", Some(AuthorCommands.KeepCurrent))
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      _ <- telegramApi.sendInlineButtons(context.chatId, text, List(keepButton))
    } yield ()
  }    
}
