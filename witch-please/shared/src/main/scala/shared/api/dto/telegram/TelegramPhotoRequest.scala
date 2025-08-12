package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramPhotoRequest(
  @jsonField("chat_id") chatId: Long,
  @jsonField("photo") photo: String
) derives JsonCodec, Schema
