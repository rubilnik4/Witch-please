package bot.infrastructure.services

import bot.application.configurations.BotConfig
import bot.infrastructure.services.sessions.{BotSessionService, BotSessionServiceLayer}
import bot.infrastructure.services.tarot.{TarotApiService, TarotApiServiceLayer}
import shared.infrastructure.services.*
import shared.infrastructure.services.files.{FileStorageService, FileStorageServiceLayer}
import shared.infrastructure.services.telegram.{TelegramApiService, TelegramApiServiceLayer}
import zio.{ZIO, ZLayer}

object BotServiceLayer {
  private val tokenLayer: ZLayer[BotConfig, Nothing, String] =
    ZLayer.fromFunction((config: BotConfig) => config.telegram.token)

  private val storedLayer: ZLayer[BotConfig, Throwable, String] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.service[BotConfig]
        localStorage <- ZIO.fromOption(config.localStorage)
          .orElseFail(new RuntimeException("Local storage config is missing"))
      } yield localStorage.path
    }
    
  val storageLayer: ZLayer[BotConfig, Throwable, FileStorageService] =
    storedLayer >>> FileStorageServiceLayer.localFileStorageServiceLive

  val telegramLayer: ZLayer[BotConfig, Throwable, TelegramApiService] =
    tokenLayer >>> TelegramApiServiceLayer.telegramApiServiceLive 

  private val tarotUrlLayer: ZLayer[BotConfig, Nothing, String] =
    ZLayer.fromFunction((config: BotConfig) => config.project.tarotUrl)

  val botServiceLive: ZLayer[BotConfig, Throwable, BotService] =
    (
      telegramLayer ++
        (tarotUrlLayer >>> TarotApiServiceLayer.tarotApiServiceLive) ++
        storageLayer ++ BotSessionServiceLayer.botSessionServiceLive
      ) >>> ZLayer.fromFunction(BotServiceLive.apply)
}
