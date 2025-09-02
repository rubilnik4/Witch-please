package bot.layers

import bot.application.configurations.AppConfig
import com.typesafe.config.ConfigFactory
import zio.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigProvider

object TestAppConfigLayer {
  val testAppConfigLive: ZLayer[Any, Config.Error, AppConfig] =
    val typesafeConfig = ConfigFactory
      .parseResources("application-test.conf")
      .resolve()
    val provider = TypesafeConfigProvider.fromTypesafeConfig(typesafeConfig)
    ZLayer.fromZIO(
      read(AppConfig.config from provider)
    )
}
