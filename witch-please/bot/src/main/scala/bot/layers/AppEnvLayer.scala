package bot.layers

import bot.application.configurations.AppConfig
import bot.application.handlers.*
import bot.application.handlers.telegram.TelegramCommandHandler
import bot.infrastructure.repositories.*
import bot.infrastructure.services.*
import zio.ZLayer

object AppEnvLayer {
  val appEnvLive: ZLayer[
    AppConfig
      & BotService
      & BotRepository
      & BotCommandHandler,
//      & TarotMeter & TarotTracing,
    Nothing,
    AppEnv
  ] =
    ZLayer.fromFunction(AppEnvLive.apply)
}
