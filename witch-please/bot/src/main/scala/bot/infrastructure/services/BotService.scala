package bot.infrastructure.services

import bot.infrastructure.repositories.BotRepository
import bot.infrastructure.repositories.sessions.BotSessionRepository
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import shared.infrastructure.services.files.*
import shared.infrastructure.services.telegram.*

trait BotService {
  def telegramApiService: TelegramChannelService
  def telegramWebhookService: TelegramWebhookService
  def fileStorageService: FileStorageService
  def tarotApiService: TarotApiService
  def botSessionService: BotSessionService
}