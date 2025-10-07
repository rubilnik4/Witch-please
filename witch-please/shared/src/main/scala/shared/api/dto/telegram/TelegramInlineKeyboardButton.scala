package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramInlineKeyboardButton(
  @jsonField("text") text: String,
  @jsonField("callback_data") callbackData: Option[String] = None,
  @jsonField("url") url: Option[String] = None,
  @jsonField("switch_inline_query") switchInlineQuery: Option[String] = None,
  @jsonField("switch_inline_query_current_chat") switchInlineQueryCurrentChat: Option[String] = None
) derives JsonCodec, Schema
