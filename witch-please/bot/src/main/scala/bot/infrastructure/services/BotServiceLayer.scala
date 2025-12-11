package bot.infrastructure.services

import bot.application.configurations.BotConfig
import bot.infrastructure.repositories.BotRepositoryLayer
import bot.infrastructure.services.sessions.{BotSessionService, BotSessionServiceLayer}
import bot.infrastructure.services.storage.LocalFileStorageLayer
import bot.infrastructure.services.tarot.*
import shared.application.configurations.TelegramConfig
import shared.infrastructure.services.*
import shared.infrastructure.services.storage.{FileStorageService, FileStorageServiceLayer}
import shared.infrastructure.services.telegram.*
import zio.ZLayer

object BotServiceLayer {
  val storageLayer: ZLayer[BotConfig, Throwable, FileStorageService] =
    LocalFileStorageLayer.storageLayer >>> FileStorageServiceLayer.localFileStorageServiceLive

  val telegramConfigLayer: ZLayer[BotConfig, Throwable, TelegramConfig] =
    ZLayer.fromFunction((config: BotConfig) => config.telegram)

  val telegramTokenLayer: ZLayer[BotConfig, Throwable, String] =
    ZLayer.fromFunction((config: BotConfig) => config.telegram.token)
    
  private val tarotUrlLayer: ZLayer[BotConfig, Nothing, TarotApiUrl] =
    ZLayer.fromFunction((config: BotConfig) => TarotApiUrl(config.project.tarotUrl))

  val live: ZLayer[BotConfig, Throwable, BotService] =
    (
      (telegramTokenLayer >>> TelegramApiServiceLayer.live) ++
      (telegramConfigLayer >>> TelegramWebhookLayer.telegramWebhookLive) ++
      (tarotUrlLayer >>> TarotApiServiceLayer.live) ++
      storageLayer ++
      (BotRepositoryLayer.live >>> BotSessionServiceLayer.live)
    ) >>> ZLayer.fromFunction(BotServiceLive.apply)
}
