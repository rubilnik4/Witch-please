package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramMediaGroupRequest(
  @jsonField("chat_id") chatId: Long,
  @jsonField("media") media: List[TelegramInputMediaPhotoRequest]
) derives JsonCodec, Schema
