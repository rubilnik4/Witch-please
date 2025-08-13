package bot.infrastructure.services

import bot.application.configurations.AppConfig
import shared.infrastructure.services.*
import zio.ZLayer

object BotServiceLayer {
  private val tokenLayer: ZLayer[AppConfig, Nothing, String] =
    ZLayer.fromFunction((config: AppConfig) => config.telegram.token)
    
  val botServiceLive: ZLayer[AppConfig, Throwable, BotService] =
    (
      tokenLayer >>> TelegramApiServiceLayer.telegramApiServiceLive
      ) >>> ZLayer.fromFunction(BotServiceLive.apply)
}
