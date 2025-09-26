package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramWebhookInfo(
  @jsonField("url") url: String,
  @jsonField("has_custom_certificate") hasCustomCertificate: Boolean,
  @jsonField("pending_update_count") pendingUpdateCount: Int,
  @jsonField("ip_address") ipAddress: Option[String] = None,
  @jsonField("last_error_date") lastErrorDate: Option[Long] = None,
  @jsonField("last_error_message") lastErrorMessage: Option[String] = None
) derives JsonCodec, Schema
