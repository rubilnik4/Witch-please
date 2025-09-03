package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramKeyboardMarkup(
  @jsonField("inline_keyboard") inlineKeyboard: List[List[TelegramKeyboardButton]]
) derives JsonCodec, Schema
