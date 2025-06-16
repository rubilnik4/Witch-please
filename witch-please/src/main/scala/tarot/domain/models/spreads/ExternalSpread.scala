package tarot.domain.models.spreads

import tarot.domain.models.photo.PhotoLocation
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.util.UUID

final case class ExternalSpread(
    title: String,
    cardCount: Integer,
    coverPhotoId: PhotoLocation)
{
  override def toString: String = s"title: $title"
}