package tarot.api.dto.telegram

import zio.json.{DeriveJsonCodec, JsonCodec, jsonField}
import zio.schema.{DeriveSchema, Schema}

final case class TelegramFile(
  @jsonField("file_id") fileId: String,
  @jsonField("file_unique_id") fileUniqueId: String,
  @jsonField("file_size") fileSize: Option[Long],
  @jsonField("file_path") filePath: String
)

object TelegramFile {
  given JsonCodec[TelegramFile] = DeriveJsonCodec.gen
  given Schema[TelegramFile] = DeriveSchema.gen
}