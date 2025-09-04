package tarot.layers

import tarot.application.configurations.TarotConfig
import zio.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigProvider

object TarotConfigLayer {
  val appConfigLive: ZLayer[Any, Config.Error, TarotConfig] = {
    val provider = TypesafeConfigProvider.fromResourcePath()
    ZLayer.fromZIO(
      read(TarotConfig.config from provider)
    )
  }
}
