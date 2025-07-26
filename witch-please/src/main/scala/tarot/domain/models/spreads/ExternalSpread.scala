package tarot.domain.models.spreads

import tarot.domain.models.photo.ExternalPhoto
import tarot.domain.models.projects.ProjectId

final case class ExternalSpread(
    projectId: ProjectId,
    title: String,
    cardCount: Integer,
    coverPhotoId: ExternalPhoto)
{
  override def toString: String = title
}