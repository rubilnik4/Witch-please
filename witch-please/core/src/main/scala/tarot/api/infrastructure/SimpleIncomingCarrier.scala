package tarot.api.infrastructure

import zio.telemetry.opentelemetry.context.IncomingContextCarrier

final case class SimpleIncomingCarrier(data: Map[String, String])
  extends IncomingContextCarrier[Map[String, String]] {

  override val kernel: Map[String, String] = data

  override def getAllKeys(carrier: Map[String, String]): Iterable[String] =
    carrier.keys

  override def getByKey(carrier: Map[String, String], key: String): Option[String] =
    carrier.get(key)
}
