package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramCommandRequest(
  @jsonField("command") command: String,
  @jsonField("description") description: String
) derives JsonCodec, Schema
