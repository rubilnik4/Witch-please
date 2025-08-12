package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramChatResponse(
  @jsonField("id") id: Long,
  @jsonField("type") `type`: String
) derives JsonCodec, Schema
