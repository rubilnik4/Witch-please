package shared.infrastructure.telemetry.metrics

import zio.telemetry.opentelemetry.metrics.*
import zio.{Ref, ZIO, ZLayer}

object TelemetryMeterLayer {
  val live: ZLayer[Meter, Nothing, TelemetryMeter] =
    ZLayer.fromZIO {
      for {
        meter <- ZIO.service[Meter]
        cache <- Ref.Synchronized.make(Map.empty[String, Histogram[Double]])
      } yield new TelemetryMeterLive(meter, cache)
    }
}
