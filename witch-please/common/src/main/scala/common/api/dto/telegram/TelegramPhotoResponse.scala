package common.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramPhotoResponse(
  @jsonField("ok") ok: Boolean,
  @jsonField("result") result: TelegramMessageResponse
) derives JsonCodec, Schema