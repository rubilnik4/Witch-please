package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramMessageRequest(
  @jsonField("chat_id") chatId: Long,
  @jsonField("text") text: String,
  @jsonField("reply_markup") replyMarkup: Option[TelegramKeyboardMarkup] = None
) derives JsonCodec, Schema
