package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramFileResultResponse(
  @jsonField("ok") ok: Boolean,
  @jsonField("result") result: TelegramFileResponse
) derives JsonCodec, Schema

final case class TelegramFileResponse(
   @jsonField("file_id") fileId: String,
   @jsonField("file_unique_id") fileUniqueId: String,
   @jsonField("file_size") fileSize: Option[Long],
   @jsonField("file_path") filePath: String
) derives JsonCodec, Schema