package bot.infrastructure.telemetry

import bot.application.configurations.BotConfig
import shared.application.configurations.TelemetryConfig
import zio.{ZIO, ZLayer}

object BotTelemetryLayer {
  val telemetryConfigLayer: ZLayer[BotConfig, Throwable, TelemetryConfig] =
    ZLayer.fromZIO {
      ZIO.serviceWithZIO[BotConfig] { config =>
        ZIO.fromOption(config.telemetry)
          .orElseFail(new NoSuchElementException("TelemetryConfig is missing in BotConfig"))
      }
    }
}
