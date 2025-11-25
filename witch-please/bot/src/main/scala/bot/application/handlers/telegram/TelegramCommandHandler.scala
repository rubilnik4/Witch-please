package bot.application.handlers.telegram

import bot.domain.models.telegram.*
import bot.layers.BotEnv
import zio.ZIO

trait TelegramCommandHandler() {
  def handle(message: TelegramMessage): ZIO[BotEnv, Throwable, Unit]
}
