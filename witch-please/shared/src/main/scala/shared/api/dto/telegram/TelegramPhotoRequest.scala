package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramPhotoRequest(
  @jsonField("chat_id") chatId: Long,
  @jsonField("photo") photo: String,
  @jsonField("caption") caption: Option[String] = None
) derives JsonCodec, Schema
