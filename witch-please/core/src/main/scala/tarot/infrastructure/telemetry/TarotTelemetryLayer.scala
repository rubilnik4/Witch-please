package tarot.infrastructure.telemetry

import shared.application.configurations.TelemetryConfig
import shared.infrastructure.telemetry.LogLevelMapper
import tarot.application.configurations.TarotConfig
import zio.telemetry.opentelemetry.OpenTelemetry
import zio.{LogLevel, ZIO, ZLayer}

object TarotTelemetryLayer {
  val telemetryConfigLayer: ZLayer[TarotConfig, Throwable, TelemetryConfig] =
    ZLayer.fromZIO {
      ZIO.serviceWithZIO[TarotConfig] { config =>
        ZIO.fromOption(config.telemetry)
          .orElseFail(new NoSuchElementException("TelemetryConfig is missing in TarotConfig"))
      }
    }
}
