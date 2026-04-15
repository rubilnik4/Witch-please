package shared.infrastructure.telemetry

import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.`export`.PeriodicMetricReader
import shared.application.configurations.*
import shared.infrastructure.telemetry.TelemetryResources
import zio.*

object MetricsLayer {
  val metricsLive: ZLayer[TelemetryConfig, Throwable, SdkMeterProvider] = ZLayer.scoped {
    for {
      telemetryConfig <- ZIO.service[TelemetryConfig]

      exporter <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          OtlpGrpcMetricExporter.builder()
            .setEndpoint(telemetryConfig.otelEndpoint)
            .build()
        )
      )

      reader <- ZIO.fromAutoCloseable(
        ZIO.succeed(PeriodicMetricReader.builder(exporter).build())
      )

      provider <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .setResource(TelemetryResources.telemetryResource(telemetryConfig.appName))
            .build()
        )
      )
    } yield provider
  }
}
