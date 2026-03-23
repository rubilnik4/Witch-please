package tarot.layers

import com.typesafe.config.ConfigFactory
import tarot.application.configurations.TarotConfig
import zio.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigProvider

object TarotConfigLayer {
  val appConfigLive: ZLayer[Any, Config.Error, TarotConfig] = {
    val provider = TypesafeConfigProvider.fromTypesafeConfig(loadConfig())
    ZLayer.fromZIO(
      read(TarotConfig.config from provider)
    )
  }

  private def loadConfig() = {
    val baseConfig = ConfigFactory.parseResources("application.conf")
    val profileConfig = currentProfile
      .filter(_.nonEmpty)
      .map(profile => ConfigFactory.parseResources(s"application.$profile.conf"))
      .getOrElse(ConfigFactory.empty())

    profileConfig.withFallback(baseConfig).resolve()
  }

  private def currentProfile: Option[String] =
    sys.env.get("APP_ENV")
      .map(_.trim.toLowerCase)
}
