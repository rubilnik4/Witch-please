package tarot.domain.models.spreads

import tarot.domain.models.photo.ExternalPhoto

final case class ExternalSpread(
    title: String,
    cardCount: Integer,
    coverPhotoId: ExternalPhoto)
{
  override def toString: String = title
}