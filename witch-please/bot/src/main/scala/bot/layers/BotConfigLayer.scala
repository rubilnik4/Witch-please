package bot.layers

import bot.application.configurations.BotConfig
import zio.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigProvider

object BotConfigLayer {
  val configLive: ZLayer[Any, Config.Error, BotConfig] = {
    val provider = TypesafeConfigProvider.fromResourcePath()
    ZLayer.fromZIO(
      read(BotConfig.config from provider)
    )
  }
}
