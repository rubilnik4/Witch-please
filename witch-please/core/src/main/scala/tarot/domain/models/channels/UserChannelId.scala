package tarot.domain.models.channels

import java.util.UUID

final case class UserChannelId(id: UUID) extends AnyVal {
  override def toString: String = id.toString
}
