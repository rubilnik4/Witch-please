package tarot.domain.models.spreads

import tarot.domain.models.photo.ExternalPhoto
import tarot.domain.models.projects.ProjectId

final case class ExternalSpread(
  title: String,
  cardCount: Integer,
  coverPhoto: ExternalPhoto
)
{
  override def toString: String = title
}