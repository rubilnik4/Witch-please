package tarot.domain.models.photo

import java.util.UUID

final case class PhotoId(id: UUID) extends AnyVal {
  override def toString: String = id.toString
}
