package shared.infrastructure.telemetry.metrics

import io.opentelemetry.api.common.{AttributeKey, Attributes}
import zio.*
import zio.telemetry.opentelemetry.metrics.*

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.TimeUnit

final class TelemetryMeterLive (meter: Meter, cache: Ref.Synchronized[Map[String, Histogram[Double]]]) extends TelemetryMeter {

  override def time[R, E, A](name: String, unit: TimeUnit, attributes: Map[String, String])(zio: ZIO[R, E, A]): ZIO[R, E, A] =
    for {
      start <- Clock.nanoTime
      result <- zio.onExit(recordOnExit(name, unit, attributes, start))
    } yield result

  private def recordOnExit[E, A](name: String, unit: TimeUnit, attributes: Map[String, String],startNs: Long)
                                (exit: Exit[E, A]): UIO[Unit] =
    for {
      end <- Clock.nanoTime
      value = toUnit(end - startNs, unit)
      histogram <- getHistogram(name)
      _ <- histogram.record(value, toAttributes(attributes, exit))
    } yield ()

  private def getHistogram(name: String): UIO[Histogram[Double]] =
    cache.modifyZIO { histograms =>
      histograms.get(name) match {
        case Some(histogram) => ZIO.succeed((histogram, histograms))
        case None =>
          meter.histogram(
            name = name,
            description = None,
            unit = None
          ).map { histogram => (histogram, histograms.updated(name, histogram)) }
      }
    }

  private def toAttributes(base: Map[String, String], exit: Exit[?, ?]): Attributes = {
    val builder = Attributes.builder()
    base.foreach { case (k, v) => builder.put(AttributeKey.stringKey(k), v) }
    builder.put(AttributeKey.stringKey("outcome"), if (exit.isSuccess) "ok" else "error")
    builder.build()
  }

  private def toUnit(durationNs: Long, unit: TimeUnit): Double =
    unit match {
      case TimeUnit.NANOSECONDS   => durationNs.toDouble
      case TimeUnit.MICROSECONDS  => durationNs / 1e3
      case TimeUnit.MILLISECONDS  => durationNs / 1e6
      case TimeUnit.SECONDS       => durationNs / 1e9
      case TimeUnit.MINUTES       => durationNs / (60 * 1e9)
      case TimeUnit.HOURS         => durationNs / (3600 * 1e9)
      case TimeUnit.DAYS          => durationNs / (86400 * 1e9)
    }
}
