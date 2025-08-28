package bot.infrastructure.services

import bot.application.configurations.AppConfig
import bot.infrastructure.services.sessions.{BotSessionService, BotSessionServiceLayer}
import bot.infrastructure.services.tarot.{TarotApiService, TarotApiServiceLayer}
import shared.infrastructure.services.*
import shared.infrastructure.services.telegram.{TelegramApiService, TelegramApiServiceLayer}
import zio.ZLayer

object BotServiceLayer {
  private val tokenLayer: ZLayer[AppConfig, Nothing, String] =
    ZLayer.fromFunction((config: AppConfig) => config.telegram.token)

  private val tarotUrlLayer: ZLayer[AppConfig, Nothing, String] =
    ZLayer.fromFunction((config: AppConfig) => config.project.tarotUrl)

  val botServiceLive: ZLayer[AppConfig, Throwable, BotService] =
    (
      (tokenLayer >>> TelegramApiServiceLayer.telegramApiServiceLive) ++
        (tarotUrlLayer >>> TarotApiServiceLayer.tarotApiServiceLive) ++
        BotSessionServiceLayer.botSessionServiceLive
      ) >>> ZLayer.fromFunction(BotServiceLive.apply)
}
