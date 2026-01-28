package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramInputMediaPhotoRequest(
  @jsonField("type") `type`: String = "photo",
  @jsonField("media") media: String,
  @jsonField("caption") caption: Option[String] = None
) derives JsonCodec, Schema
