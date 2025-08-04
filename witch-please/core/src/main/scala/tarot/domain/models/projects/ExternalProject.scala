package tarot.domain.models.projects

import java.time.Instant

case class ExternalProject(
  name: String
)
{
  override def toString: String = name
}