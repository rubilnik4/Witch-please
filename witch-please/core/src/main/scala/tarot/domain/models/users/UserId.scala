package tarot.domain.models.users

import java.util.UUID

final case class UserId(id: UUID) extends AnyVal {
  override def toString: String = id.toString
}
