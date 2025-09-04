package bot.infrastructure.services

import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import shared.infrastructure.services.files.FileStorageService
import shared.infrastructure.services.telegram.TelegramApiService

final case class BotServiceLive(
  telegramApiService: TelegramApiService,
  fileStorageService: FileStorageService,
  tarotApiService: TarotApiService,
  botSessionService: BotSessionService
) extends BotService
