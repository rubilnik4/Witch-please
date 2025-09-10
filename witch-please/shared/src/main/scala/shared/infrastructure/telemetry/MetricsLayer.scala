package shared.infrastructure.telemetry

import io.opentelemetry.exporter.prometheus.PrometheusHttpServer
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import shared.application.configurations.*
import shared.infrastructure.telemetry.TelemetryResources
import zio.*

object MetricsLayer {
  val metricsLive: ZLayer[TelemetryConfig, Throwable, SdkMeterProvider] = ZLayer.scoped {
    for {
      telemetryConfig <- ZIO.service[TelemetryConfig]
      
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
            .setResource(TelemetryResources.telemetryResource(telemetryConfig.appName))
            .build()
        )
      )
    } yield provider
  }
}
