package bot.layers

import bot.application.configurations.BotConfig
import bot.application.handlers.*
import bot.application.handlers.telegram.TelegramCommandHandler
import bot.infrastructure.repositories.*
import bot.infrastructure.services.*
import zio.ZLayer

object BotEnvLayer {
  val envLive: ZLayer[
    BotConfig
      & BotService
      & BotRepository
      & BotCommandHandler,
//      & TarotMeter & TarotTracing,
    Nothing,
    BotEnv
  ] =
    ZLayer.fromFunction(BotEnvLive.apply)
}
