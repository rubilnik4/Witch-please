package tarot.infrastructure.telemetry

import io.opentelemetry.api
import io.opentelemetry.api.OpenTelemetry as OtelSdk
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.*
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.trace.SdkTracerProvider
import tarot.application.configurations.TarotConfig

import zio.*
import zio.telemetry.opentelemetry.OpenTelemetry
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing

object TelemetryLayer {
  private val otelSdkLive: ZLayer[TarotConfig, Throwable, OpenTelemetrySdk] =
    (TracingLayer.tracingLive ++ MetricsLayer.metricsLive ++ LoggingLayer.loggingLive) >>>
      ZLayer.scoped {
        ZIO.fromAutoCloseable(
          for {
            tracing <- ZIO.service[SdkTracerProvider]
            metrics <- ZIO.service[SdkMeterProvider]
            logging <- ZIO.service[SdkLoggerProvider]
            sdk <- ZIO.succeed(
              OpenTelemetrySdk.builder()
                .setTracerProvider(tracing)
                .setMeterProvider(metrics)
                .setLoggerProvider(logging)
                .build()
            )
          } yield sdk
        )
      }

  private val tracingLayer: URLayer[OtelSdk & ContextStorage, Tracing] =
    OpenTelemetry.tracing(TelemetryResources.telemetryAppName)

  private val meteringLayer: URLayer[OtelSdk & ContextStorage, Meter] =
    OpenTelemetry.metrics(TelemetryResources.telemetryAppName)

  private def loggingLayer: ZLayer[OtelSdk & ContextStorage & TarotConfig, Throwable, Unit] =
    ZLayer.scoped {
      for {       
        telemetryConfig <- TelemetryResources.getTelemetryConfig
        logLevel <- ZIO.fromOption(LogLevelMapper.parseLogLevel(telemetryConfig.logLevel))
          .orElseFail(new RuntimeException(s"Invalid log level: ${telemetryConfig.logLevel}"))

        _ <- OpenTelemetry.logging(TelemetryResources.telemetryAppName, logLevel).build
      } yield ()
    }

  private val contextLayer: ULayer[ContextStorage] =
    OpenTelemetry.contextZIO

  val telemetryLive: ZLayer[TarotConfig, Throwable, Meter & Tracing] =
    (otelSdkLive ++ contextLayer ++ ZLayer.environment[TarotConfig]) >>>
      (meteringLayer ++ loggingLayer ++ tracingLayer)
}
