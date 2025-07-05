package tarot.api.dto.telegram

import tarot.api.dto.tarot.telegram.TelegramSpreadCreateRequest
import zio.json.{DeriveJsonCodec, JsonCodec, jsonField}
import zio.schema.{DeriveSchema, Schema}
import zio.json._
import zio.schema._

final case class TelegramFileResponse(
  @jsonField("ok") ok: Boolean,
  @jsonField("result") result: TelegramFile
) derives JsonCodec, Schema

final case class TelegramFile(
   @jsonField("file_id") fileId: String,
   @jsonField("file_unique_id") fileUniqueId: String,
   @jsonField("file_size") fileSize: Option[Long],
   @jsonField("file_path") filePath: String
) derives JsonCodec, Schema