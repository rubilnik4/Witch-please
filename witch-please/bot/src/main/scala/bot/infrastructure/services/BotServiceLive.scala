package bot.infrastructure.services


final case class BotServiceLive(
  telegramApiService: TelegramApiService,
) extends BotService
