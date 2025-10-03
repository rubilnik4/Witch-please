package shared.infrastructure.telemetry.logging

import shared.application.configurations.TelemetryConfig
import shared.infrastructure.telemetry.LogLevelMapper
import zio.{LogLevel, ULayer, ZIO, ZLayer}
import zio.logging.{ConsoleLoggerConfig, LogFilter, LogFormat, consoleLogger}

object LoggerLayer {
  val consoleLoggerLive: ZLayer[TelemetryConfig, Throwable, Unit] =
    ZLayer.fromZIO {
      for {
        logLevel <- logLevel
      } yield consoleLogger(ConsoleLoggerConfig(
          format = LogFormat.colored,
          filter = LogFilter.LogLevelByNameConfig(logLevel)
        ))
    }

  def logLevel: ZIO[TelemetryConfig, Throwable, LogLevel] =
    for {
      telemetryConfig <- ZIO.service[TelemetryConfig]
      logLevel <- ZIO.fromOption(LogLevelMapper.parseLogLevel(telemetryConfig.logLevel))
        .orElseFail(new RuntimeException(s"Invalid log level: ${telemetryConfig.logLevel}"))
    } yield logLevel
}
