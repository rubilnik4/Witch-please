package tarot.infrastructure.telemetry

import TelemetryResources.*
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import tarot.application.configurations.AppConfig
import zio.*

object TracingLayer {
  val tracingLive: ZLayer[AppConfig, Throwable, SdkTracerProvider] = ZLayer.scoped {
    for {
      appConfig <- ZIO.service[AppConfig]
      telemetryConfig <- getTelemetryConfig

      exporter <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          OtlpGrpcSpanExporter.builder()
            .setEndpoint(telemetryConfig.otelEndpoint)
            .build()
        )
      )

      processor <- ZIO.fromAutoCloseable(
        ZIO.succeed(SimpleSpanProcessor.create(exporter))
      )

      provider <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          SdkTracerProvider.builder()
            .setResource(telemetryResource)
            .addSpanProcessor(processor)
            .build()
        )
      )
    } yield provider
  }
}
