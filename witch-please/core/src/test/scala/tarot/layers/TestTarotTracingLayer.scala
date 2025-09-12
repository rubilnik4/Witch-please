package tarot.layers

import io.opentelemetry.api.OpenTelemetry as OtelSdk
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import shared.infrastructure.telemetry.TelemetryResources.{telemetryAppName, telemetryResource}
import shared.infrastructure.telemetry.tracing.{TelemetryTracing, TelemetryTracingLayer}
import zio.*
import zio.telemetry.opentelemetry.OpenTelemetry
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.tracing.Tracing

object TestTarotTracingLayer {
  private val tracingLive: ZLayer[Any, Throwable, SdkTracerProvider] = ZLayer.scoped {
    for {
      exporter <- ZIO.fromAutoCloseable(
        ZIO.succeed(OtlpJsonLoggingSpanExporter.create())
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

  private val otelSdkLive: ZLayer[Any, Throwable, OpenTelemetrySdk] =
    tracingLive >>>
      ZLayer.scoped {
        ZIO.fromAutoCloseable(
          for {
            tracing <- ZIO.service[SdkTracerProvider]
            sdk <- ZIO.succeed(
              OpenTelemetrySdk.builder()
                .setTracerProvider(tracing)
                .build()
            )
          } yield sdk
        )
      }

  private val tracingLayer: URLayer[OtelSdk & ContextStorage, Tracing] =
    OpenTelemetry.tracing(telemetryAppName)

  private val contextLayer: ULayer[ContextStorage] =
    OpenTelemetry.contextZIO

  val tarotTracingLive: ZLayer[Any, Throwable, TelemetryTracing] =
    (otelSdkLive ++ contextLayer) >>> 
      tracingLayer >>>
      TelemetryTracingLayer.telemetryTracingLive
}

