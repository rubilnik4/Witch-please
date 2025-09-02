package bot.layers

import bot.application.handlers.BotCommandHandlerLayer
import bot.infrastructure.repositories.BotRepositoryLayer
import bot.infrastructure.services.BotServiceLayer
import zio.ZLayer

object TestAppEnvLayer {
  val testAppEnvLive: ZLayer[Any, Throwable, AppEnv] =
    ZLayer.make[AppEnv](
      TestAppConfigLayer.testAppConfigLive,
      BotServiceLayer.botServiceLive,
      BotRepositoryLayer.botRepositoryLive,
      BotCommandHandlerLayer.botCommandHandlerLive,
      AppEnvLayer.appEnvLive
    )
}
