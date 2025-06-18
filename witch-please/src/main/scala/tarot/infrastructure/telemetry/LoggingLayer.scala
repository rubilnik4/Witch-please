package tarot.infrastructure.telemetry

import TelemetryResources.*
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.sdk.logs.*
import io.opentelemetry.sdk.logs.`export`.*
import tarot.application.configurations.AppConfig
import zio.*

object LoggingLayer {
  val loggingLive: ZLayer[AppConfig, Throwable, SdkLoggerProvider] = ZLayer.scoped {
    for {
      appConfig <- ZIO.service[AppConfig]
      telemetryConfig <- getTelemetryConfig

      exporter <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          OtlpGrpcLogRecordExporter.builder()
            .setEndpoint(telemetryConfig.otelEndpoint)
            .build()
        )
      )

      processor <- ZIO.fromAutoCloseable(
        ZIO.succeed(SimpleLogRecordProcessor.create(exporter))
      )

      provider <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          SdkLoggerProvider.builder()
            .setResource(telemetryResource)
            .addLogRecordProcessor(processor)
            .build()
        )
      )
    } yield provider
  }
}
