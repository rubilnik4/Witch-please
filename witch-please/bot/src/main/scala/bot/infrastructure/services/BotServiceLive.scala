package bot.infrastructure.services

import shared.infrastructure.services.telegram.TelegramApiService


final case class BotServiceLive(
  telegramApiService: TelegramApiService,
) extends BotService
