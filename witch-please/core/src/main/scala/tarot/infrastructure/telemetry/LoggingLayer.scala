package tarot.infrastructure.telemetry

import TelemetryResources.*
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.sdk.logs.*
import io.opentelemetry.sdk.logs.`export`.*
import tarot.application.configurations.TarotConfig
import zio.*

object LoggingLayer {
  val loggingLive: ZLayer[TarotConfig, Throwable, SdkLoggerProvider] = ZLayer.scoped {
    for {
      appConfig <- ZIO.service[TarotConfig]
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
