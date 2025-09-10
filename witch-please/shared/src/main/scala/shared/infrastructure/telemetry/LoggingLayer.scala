package shared.infrastructure.telemetry

import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.sdk.logs.*
import io.opentelemetry.sdk.logs.`export`.*
import shared.application.configurations.*
import shared.infrastructure.telemetry.TelemetryResources
import zio.*

object LoggingLayer {
  val loggingLive: ZLayer[TelemetryConfig, Throwable, SdkLoggerProvider] = ZLayer.scoped {
    for {
      telemetryConfig <- ZIO.service[TelemetryConfig]

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
            .setResource(TelemetryResources.telemetryResource(telemetryConfig.appName))
            .addLogRecordProcessor(processor)
            .build()
        )
      )
    } yield provider
  }
}
