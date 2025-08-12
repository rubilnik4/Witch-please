package bot.infrastructure.services

import bot.infrastructure.services.repositories.BotSessionRepository
import bot.infrastructure.services.tarot.TarotApiService
import shared.infrastructure.services.TelegramApiService

trait BotService {
  def telegramApiService: TelegramApiService
  def tarotApiService: TarotApiService
  def botSessionRepository: BotSessionRepository
}