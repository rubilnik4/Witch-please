package tarot.api.dto.telegram

import zio.json.{DeriveJsonCodec, JsonCodec, jsonField}
import zio.schema.{DeriveSchema, Schema}
import zio.json._
import zio.schema._

final case class TelegramErrorResponse(
  @jsonField("ok") ok: Boolean,
  @jsonField("error_code") errorCode: Int,
  @jsonField("description") description: String
) derives JsonCodec, Schema
