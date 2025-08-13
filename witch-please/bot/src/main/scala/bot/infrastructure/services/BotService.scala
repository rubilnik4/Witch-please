package bot.infrastructure.services

import bot.infrastructure.repositories.BotRepository
import bot.infrastructure.repositories.sessions.BotSessionRepository
import bot.infrastructure.services.tarot.TarotApiService
import shared.infrastructure.services.telegram.TelegramApiService

trait BotService {
  def telegramApiService: TelegramApiService
  def tarotApiService: TarotApiService
  def botRepository: BotRepository
}