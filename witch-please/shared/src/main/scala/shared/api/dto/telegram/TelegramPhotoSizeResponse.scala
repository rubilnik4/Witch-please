package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramPhotoSizeResponse(
  @jsonField("file_id") fileId: String,
  @jsonField("file_unique_id") fileUniqueId: String,
  @jsonField("width") width: Int,
  @jsonField("height") height: Int,
  @jsonField("file_size") fileSize: Option[Long]
) derives JsonCodec, Schema
