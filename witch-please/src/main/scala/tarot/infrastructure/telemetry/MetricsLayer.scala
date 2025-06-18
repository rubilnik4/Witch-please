package tarot.infrastructure.telemetry

import TelemetryResources.{getTelemetryConfig, telemetryResource}
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import tarot.application.configurations.AppConfig
import zio.*

object MetricsLayer {
  val metricsLive: ZLayer[AppConfig, Throwable, SdkMeterProvider] = ZLayer.scoped {
    for {
      appConfig <- ZIO.service[AppConfig]
      telemetryConfig <- getTelemetryConfig
      port <- ZIO.attempt(telemetryConfig.prometheusPort.toInt)
        .orElseFail(new RuntimeException(s"Invalid prometheus port: ${telemetryConfig.prometheusPort}"))

      prometheusServer <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          PrometheusHttpServer.builder()
            .setHost("0.0.0.0")
            .setPort(port)
            .build()
        )
      )
      
      provider <- ZIO.fromAutoCloseable(
        ZIO.succeed(
          SdkMeterProvider.builder()
            .registerMetricReader(prometheusServer)
            .setResource(telemetryResource)
            .build()
        )
      )
    } yield provider
  }
}
