package bot.application.handlers

import bot.application.handlers.telegram.*

trait BotCommandHandler {
  def telegramCommandHandler: TelegramCommandHandler
}