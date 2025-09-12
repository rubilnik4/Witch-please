package tarot.layers

import shared.infrastructure.telemetry.metrics.TelemetryMeter
import zio.{UIO, ZIO, ZLayer}

object TestTarotMeterLayer {
  private val testMarketMeter: TelemetryMeter = new TelemetryMeter {
    override def recordSpreadDuration[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] = zio
  }

  val testMarketMeterLive: ZLayer[Any, Nothing, TelemetryMeter] =
    ZLayer.succeed(testMarketMeter)
}
