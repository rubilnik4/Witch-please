package tarot.layers

import tarot.application.telemetry.metrics.TarotMeter
import zio.{UIO, ZIO, ZLayer}

object TestTarotMeterLayer {
  private val testMarketMeter: TarotMeter = new TarotMeter {
    override def recordSpreadDuration[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] = zio
  }

  val testMarketMeterLive: ZLayer[Any, Nothing, TarotMeter] =
    ZLayer.succeed(testMarketMeter)
}
