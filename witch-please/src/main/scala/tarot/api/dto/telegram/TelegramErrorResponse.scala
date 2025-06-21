package tarot.api.dto.telegram

import zio.json.{DeriveJsonCodec, JsonCodec, jsonField}
import zio.schema.{DeriveSchema, Schema}

final case class TelegramErrorResponse(
  @jsonField("ok") ok: Boolean,
  @jsonField("error_code") errorCode: Int,
  @jsonField("description") description: String
)

object TelegramErrorResponse {
  given JsonCodec[TelegramErrorResponse] = DeriveJsonCodec.gen
  given Schema[TelegramErrorResponse] = DeriveSchema.gen
}
