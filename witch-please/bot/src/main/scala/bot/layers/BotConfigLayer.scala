package bot.layers

import bot.application.configurations.BotConfig
import com.typesafe.config.ConfigFactory
import zio.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigProvider

object BotConfigLayer {
  val configLive: ZLayer[Any, Config.Error, BotConfig] = {
    val provider = TypesafeConfigProvider.fromTypesafeConfig(loadConfig())
    ZLayer.fromZIO(
      read(BotConfig.config.from(provider))
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
