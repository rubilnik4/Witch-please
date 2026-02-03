package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramBotResponse(
  @jsonField("id") id: Long,
  @jsonField("is_bot") isBot: Boolean,
  @jsonField("first_name") firstName: String,
  @jsonField("username") username: Option[String]
) derives JsonCodec, Schema
