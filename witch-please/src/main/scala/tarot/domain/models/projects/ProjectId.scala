package tarot.domain.models.projects

import java.util.UUID

final case class ProjectId(id: UUID) extends AnyVal {
  override def toString: String = id.toString
}
