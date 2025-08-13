package bot.application.handlers

import bot.api.dto.TelegramWebhookRequest
import bot.application.commands.BotCommand
import bot.domain.models.telegram.*
import bot.infrastructure.services.authorize.SecretService
import bot.layers.AppEnv
import shared.api.dto.tarot.projects.*
import shared.api.dto.tarot.spreads.*
import shared.api.dto.tarot.users.*
import zio.ZIO

trait TelegramCommandHandler() {
  def handle(message: TelegramMessage): ZIO[AppEnv, Throwable, Unit]
}
