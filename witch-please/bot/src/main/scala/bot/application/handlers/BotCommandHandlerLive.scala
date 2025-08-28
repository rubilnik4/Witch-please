package bot.application.handlers

import bot.application.handlers.telegram.TelegramCommandHandler

final case class BotCommandHandlerLive(
  telegramCommandHandler: TelegramCommandHandler
) extends BotCommandHandler
