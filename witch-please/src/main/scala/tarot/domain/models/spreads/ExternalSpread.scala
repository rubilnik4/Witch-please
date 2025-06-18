package tarot.domain.models.spreads

import tarot.domain.models.photo.PhotoSource
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.util.UUID

final case class ExternalSpread(
    title: String,
    cardCount: Integer,
    coverPhotoId: PhotoSource)
{
  override def toString: String = s"title: $title"
}