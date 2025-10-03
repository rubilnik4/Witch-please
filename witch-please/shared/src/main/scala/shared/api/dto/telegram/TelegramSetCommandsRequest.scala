package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramSetCommandsRequest(
  commands: List[TelegramCommandRequest]
) derives JsonCodec, Schema
