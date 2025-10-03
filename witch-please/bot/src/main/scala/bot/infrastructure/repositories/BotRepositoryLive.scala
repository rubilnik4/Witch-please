package bot.infrastructure.repositories

import bot.infrastructure.repositories.sessions.BotSessionRepository
import shared.infrastructure.services.telegram.TelegramApiService

final case class BotRepositoryLive(
  botSessionRepository: BotSessionRepository,
) extends BotRepository
