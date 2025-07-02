package tarot.domain.models.cards

import tarot.domain.models.contracts.SpreadId
import tarot.domain.models.photo.{ExternalPhotoSource, PhotoSource}
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.util.UUID

final case class ExternalCard(                             
    index: Int,
    spreadId: SpreadId,                         
    description: String,
    coverPhotoId: ExternalPhotoSource)
{
  override def toString: String = s"card number $index from spread $spreadId"
}