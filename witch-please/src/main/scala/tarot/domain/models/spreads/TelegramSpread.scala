package tarot.domain.models.spreads

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.util.UUID

final case class TelegramSpread(
    title: String,
    cardCount: Integer,
    coverPhotoId: String)