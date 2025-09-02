package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramResponse[T](
  @jsonField("ok") ok: Boolean,
  @jsonField("result") result: Option[T],
  @jsonField("error_code") errorCode: Option[Int] = None,
  @jsonField("description") description: Option[String] = None
) derives JsonCodec, Schema
