package bot.infrastructure.services

import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import shared.infrastructure.services.storage.{FileStorageService, ResourceFileService}
import shared.infrastructure.services.telegram.*

final case class BotServiceLive(
  telegramApiService: TelegramApiService,
  telegramWebhookService: TelegramWebhookService,
  resourceFileService: ResourceFileService,
  tarotApiService: TarotApiService,
  botSessionService: BotSessionService
) extends BotService
