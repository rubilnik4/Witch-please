package shared.infrastructure.telemetry

import io.opentelemetry.sdk.OpenTelemetrySdk
import shared.application.configurations.TelemetryConfig
import shared.infrastructure.telemetry.TelemetryLayer.meteringLayer
import zio.*
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing

object StubTelemetryLayer {
  private val otelSdkLive: ZLayer[TelemetryConfig, Throwable, OpenTelemetrySdk] =
      ZLayer.scoped {
        ZIO.fromAutoCloseable(
          for {
            sdk <- ZIO.succeed(
              OpenTelemetrySdk.builder()
                .build()
            )
          } yield sdk
        )
      }

  val telemetryLive: ZLayer[TelemetryConfig, Throwable, Meter & Tracing] =
    (otelSdkLive ++ TelemetryLayer.contextLayer) >>>
      (TelemetryLayer.meteringLayer ++ TelemetryLayer.tracingLayer)
}

