package shared.infrastructure.telemetry.metrics

import zio.{Trace, ZIO, ZIOAspect}

import java.util.concurrent.TimeUnit

trait TelemetryMeter { self =>
  def time[R, E, A](name: String, unit: TimeUnit = TimeUnit.MILLISECONDS, attributes: Map[String, String] = Map.empty)
                   (zio: ZIO[R, E, A]): ZIO[R, E, A]

  object aspects {
    def timed(name: String, unit: TimeUnit = TimeUnit.MILLISECONDS, attributes: Map[String, String]): ZIOAspect[Nothing, Any, Nothing, Any, Any, Any] =
      new ZIOAspect[Nothing, Any, Nothing, Any, Any, Any] {
        override def apply[R, E, A](zio: ZIO[R, E, A])(implicit trace: Trace): ZIO[R, E, A] =
          self.time(name, unit, attributes)(zio)
      }
  }
}