package bot.infrastructure.services

import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import shared.infrastructure.services.files.FileStorageService
import shared.infrastructure.services.telegram.*

final case class BotServiceLive(
                                 telegramApiService: TelegramApiService,
                                 telegramWebhookService: TelegramWebhookService,
                                 fileStorageService: FileStorageService,
                                 tarotApiService: TarotApiService,
                                 botSessionService: BotSessionService
) extends BotService
