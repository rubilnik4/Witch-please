package bot.infrastructure.services

import bot.application.configurations.BotConfig
import bot.infrastructure.repositories.BotRepositoryLayer
import bot.infrastructure.services.sessions.{BotSessionService, BotSessionServiceLayer}
import bot.infrastructure.services.tarot.*
import shared.application.configurations.TelegramConfig
import shared.infrastructure.services.*
import shared.infrastructure.services.storage.*
import shared.infrastructure.services.telegram.*
import zio.ZLayer

object BotServiceLayer {
  val telegramConfigLayer: ZLayer[BotConfig, Throwable, TelegramConfig] =
    ZLayer.fromFunction((config: BotConfig) => config.telegram)

  private val telegramTokenLayer: ZLayer[BotConfig, Throwable, String] =
    ZLayer.fromFunction((config: BotConfig) => config.telegram.token)
    
  private val tarotUrlLayer: ZLayer[BotConfig, Nothing, TarotApiUrl] =
    ZLayer.fromFunction((config: BotConfig) => TarotApiUrl(config.project.tarotUrl))

  val live: ZLayer[BotConfig, Throwable, BotService] =
    (
      (telegramTokenLayer >>> TelegramApiServiceLayer.live) ++
      (telegramConfigLayer >>> TelegramWebhookLayer.telegramWebhookLive) ++
      (tarotUrlLayer >>> TarotApiServiceLayer.live) ++
      ResourceFileServiceLayer.live ++
      (BotRepositoryLayer.live >>> BotSessionServiceLayer.live)
    ) >>> ZLayer.fromFunction(BotServiceLive.apply)
}
