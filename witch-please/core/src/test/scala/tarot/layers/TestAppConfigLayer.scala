package tarot.layers

import com.typesafe.config.ConfigFactory
import tarot.application.configurations.TarotConfig
import zio.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigProvider

object TestAppConfigLayer {
  val testAppConfigLive: ZLayer[Any, Config.Error, TarotConfig] =
    val typesafeConfig = ConfigFactory
      .parseResources("application-test.conf")
      .resolve()
    val provider = TypesafeConfigProvider.fromTypesafeConfig(typesafeConfig)
    ZLayer.fromZIO(
      read(TarotConfig.config from provider)
    )
}
