package shared.infrastructure.telemetry

import io.opentelemetry.api
import io.opentelemetry.api.OpenTelemetry as OtelSdk
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.*
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.trace.SdkTracerProvider
import shared.application.configurations.TelemetryConfig
import zio.*
import zio.telemetry.opentelemetry.OpenTelemetry
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.metrics.Meter
import zio.telemetry.opentelemetry.tracing.Tracing

object TelemetryLayer {
  private val otelSdkLive: ZLayer[TelemetryConfig, Throwable, OpenTelemetrySdk] =
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

  private val tracingLayer: URLayer[OtelSdk & ContextStorage & TelemetryConfig, Tracing] =
    ZLayer.scoped(
      ZIO.serviceWithZIO[TelemetryConfig](cfg =>
        OpenTelemetry.tracing(cfg.appName).build.map(_.get[Tracing])
      )
    )

  private val meteringLayer: URLayer[OtelSdk & ContextStorage & TelemetryConfig, Meter] =
    ZLayer.scoped(
      ZIO.serviceWithZIO[TelemetryConfig](cfg =>
        OpenTelemetry.metrics(cfg.appName).build.map(_.get[Meter])
      )
    )

  private def loggingLayer: ZLayer[OtelSdk & ContextStorage & TelemetryConfig, Throwable, Unit] =
    ZLayer.scoped {
      for {
        telemetryConfig <- ZIO.service[TelemetryConfig]
        logLevel <- ZIO.fromOption(LogLevelMapper.parseLogLevel(telemetryConfig.logLevel))
          .orElseFail(new RuntimeException(s"Invalid log level: ${telemetryConfig.logLevel}"))

        _ <- OpenTelemetry.logging(telemetryConfig.appName, logLevel).build
      } yield ()
    }

  private val contextLayer: ULayer[ContextStorage] =
    OpenTelemetry.contextZIO

  val telemetryLive: ZLayer[TelemetryConfig, Throwable, Meter & Tracing] =
    (otelSdkLive ++ contextLayer ++ ZLayer.environment[TelemetryConfig]) >>>
      (meteringLayer ++ loggingLayer ++ tracingLayer)
}
