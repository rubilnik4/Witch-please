package tarot.domain.models.cards

import java.util.UUID

final case class CardId(id: UUID) extends AnyVal {
  override def toString: String = id.toString
}
