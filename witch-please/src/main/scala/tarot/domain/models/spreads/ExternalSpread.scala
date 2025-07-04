package tarot.domain.models.spreads

import tarot.domain.models.photo.{ExternalPhoto, Photo}
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.util.UUID

final case class ExternalSpread(
    title: String,
    cardCount: Integer,
    coverPhotoId: ExternalPhoto)
{
  override def toString: String = s"spread with title: $title"
}