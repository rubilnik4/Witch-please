package bot.infrastructure.services

import bot.application.configurations.BotConfig
import bot.infrastructure.repositories.BotRepositoryLayer
import bot.infrastructure.repositories.sessions.BotSessionRepositoryLayer
import bot.infrastructure.services.sessions.{BotSessionService, BotSessionServiceLayer}
import bot.infrastructure.services.storage.LocalFileStorageLayer
import bot.infrastructure.services.tarot.{TarotApiService, TarotApiServiceLayer}
import shared.application.configurations.TelegramConfig
import shared.infrastructure.services.*
import shared.infrastructure.services.storage.{FileStorageService, FileStorageServiceLayer}
import shared.infrastructure.services.telegram.*
import zio.{ZIO, ZLayer}

object BotServiceLayer {
  val storageLayer: ZLayer[BotConfig, Throwable, FileStorageService] =
    LocalFileStorageLayer.storageLayer >>> FileStorageServiceLayer.localFileStorageServiceLive

  val telegramConfigLayer: ZLayer[BotConfig, Throwable, TelegramConfig] =
    ZLayer.fromFunction((config: BotConfig) => config.telegram)

  val telegramTokenLayer: ZLayer[BotConfig, Throwable, String] =
    ZLayer.fromFunction((config: BotConfig) => config.telegram.token)
    
  private val tarotUrlLayer: ZLayer[BotConfig, Nothing, String] =
    ZLayer.fromFunction((config: BotConfig) => config.project.tarotUrl)

  val botServiceLive: ZLayer[BotConfig, Throwable, BotService] =
    (
      (telegramTokenLayer >>> TelegramApiServiceLayer.telegramChannelServiceLive) ++
      (telegramConfigLayer >>> TelegramWebhookLayer.telegramWebhookLive) ++
      (tarotUrlLayer >>> TarotApiServiceLayer.tarotApiServiceLive) ++
      storageLayer ++
      (BotRepositoryLayer.live >>> BotSessionServiceLayer.live)
    ) >>> ZLayer.fromFunction(BotServiceLive.apply)
}
