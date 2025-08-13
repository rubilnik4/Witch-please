package bot.infrastructure.services

import shared.infrastructure.services.TelegramApiService


final case class BotServiceLive(
  telegramApiService: TelegramApiService,
) extends BotService
