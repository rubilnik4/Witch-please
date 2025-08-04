package tarot.domain.models.cards

import tarot.domain.models.photo.ExternalPhoto
import tarot.domain.models.spreads.SpreadId

final case class ExternalCard(                             
  index: Int,
  spreadId: SpreadId,
  description: String,
  coverPhotoId: ExternalPhoto)
{
  override def toString: String = s"number $index from spread $spreadId"
}