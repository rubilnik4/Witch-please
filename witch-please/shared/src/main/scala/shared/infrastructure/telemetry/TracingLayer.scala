package shared.infrastructure.telemetry

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import shared.application.configurations.TelemetryConfig
import shared.infrastructure.telemetry.TelemetryResources.*
import zio.*

object TracingLayer {
  val tracingLive: ZLayer[TelemetryConfig, Throwable, SdkTracerProvider] = ZLayer.scoped {
    for {
      telemetryConfig <- ZIO.service[TelemetryConfig]

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
            .setResource(TelemetryResources.telemetryResource(telemetryConfig.appName))
            .addSpanProcessor(processor)
            .build()
        )
      )
    } yield provider
  }
}
