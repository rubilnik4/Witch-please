package bot.layers

import bot.application.configurations.BotConfig
import com.typesafe.config.ConfigFactory
import zio.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigProvider

object TestBotConfigLayer {
  val testBotConfigLive: ZLayer[Any, Config.Error, BotConfig] =
    val typesafeConfig = ConfigFactory
      .parseResources("application-test.conf")
      .resolve()
    val provider = TypesafeConfigProvider.fromTypesafeConfig(typesafeConfig)
    ZLayer.fromZIO(
      read(BotConfig.config from provider)
    )
}
