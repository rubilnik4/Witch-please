package tarot.application.telemetry.metrics

import zio.{Trace, ZIO, ZIOAspect}

trait TarotMeter { self =>
  def recordSpreadDuration[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A]

  object aspects {
    def spreadDuration[E1, A1]: ZIOAspect[Nothing, Any, Nothing, E1, A1, A1] =
      new ZIOAspect[Nothing, Any, Nothing, E1, A1, A1] {
        override def apply[R, E <: E1, A <: A1](zio: ZIO[R, E, A])(implicit trace: Trace): ZIO[R, E, A] =
          self.recordSpreadDuration(zio)
      }
  }
}