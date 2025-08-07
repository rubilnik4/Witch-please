package common.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramMessageResponse(
  @jsonField("message_id") messageId: Long,
  @jsonField("chat") chat: TelegramChatResponse,
  @jsonField("date") date: Long,
  @jsonField("photo") photo: List[TelegramPhotoSizeResponse]
) derives JsonCodec, Schema
