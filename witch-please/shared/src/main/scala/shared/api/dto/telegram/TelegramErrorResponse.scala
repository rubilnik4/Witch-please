package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramErrorResponse(
  @jsonField("ok") ok: Boolean,
  @jsonField("error_code") errorCode: Int,
  @jsonField("description") description: String
) derives JsonCodec, Schema
