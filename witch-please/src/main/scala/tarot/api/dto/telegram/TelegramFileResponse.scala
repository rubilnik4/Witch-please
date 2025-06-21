package tarot.api.dto.telegram

import tarot.api.dto.TelegramSpreadRequest
import zio.json.{DeriveJsonCodec, JsonCodec, jsonField}
import zio.schema.{DeriveSchema, Schema}

final case class TelegramFileResponse(
  @jsonField("ok") ok: Boolean,
  @jsonField("result") result: TelegramFile
)

object TelegramFileResponse {
  given JsonCodec[TelegramFileResponse] = DeriveJsonCodec.gen
  given Schema[TelegramFileResponse] = DeriveSchema.gen
}