package tarot.domain.models.spreads

import tarot.domain.models.photo.{ExternalPhotoSource, PhotoSource}
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.util.UUID

final case class ExternalSpread(
    title: String,
    cardCount: Integer,
    coverPhotoId: ExternalPhotoSource)
{
  override def toString: String = s"spread with title: $title"
}