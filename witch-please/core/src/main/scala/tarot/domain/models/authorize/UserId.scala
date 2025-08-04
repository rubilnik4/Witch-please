package tarot.domain.models.authorize

import java.util.UUID

final case class UserId(id: UUID) extends AnyVal {
  override def toString: String = id.toString
}
