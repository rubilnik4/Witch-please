package bot.layers

import bot.application.handlers.BotCommandHandlerLayer
import bot.infrastructure.repositories.BotRepositoryLayer
import zio.ZLayer

object TestAppEnvLayer {
  val testAppEnvLive: ZLayer[Any, Throwable, BotEnv] =
    ZLayer.make[BotEnv](
      TestAppConfigLayer.testAppConfigLive,
      TestBotServiceLayer.botServiceLive,
      BotRepositoryLayer.botRepositoryLive,
      BotCommandHandlerLayer.botCommandHandlerLive,
      BotEnvLayer.envLive
    )
}
