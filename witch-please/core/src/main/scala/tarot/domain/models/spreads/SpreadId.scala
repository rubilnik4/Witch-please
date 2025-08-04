package tarot.domain.models.spreads

import java.util.UUID

final case class SpreadId(id: UUID) extends AnyVal {
  override def toString: String = id.toString
}
