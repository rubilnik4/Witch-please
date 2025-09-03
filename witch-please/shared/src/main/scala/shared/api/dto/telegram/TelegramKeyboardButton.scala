package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramKeyboardButton(
  @jsonField("text") text: String,
  @jsonField("callback_data") callbackData: Option[String] = None
) derives JsonCodec, Schema
