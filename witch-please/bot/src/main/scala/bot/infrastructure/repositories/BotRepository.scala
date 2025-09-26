package bot.infrastructure.repositories

import bot.infrastructure.repositories.sessions.BotSessionRepository
import bot.infrastructure.services.tarot.TarotApiService
import shared.infrastructure.services.telegram.TelegramChannelService

trait BotRepository {
  def botSessionRepository: BotSessionRepository
}