package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramSetWebhookRequest(
  @jsonField("url") url: String,
  @jsonField("secret_token") secretToken: String,
  @jsonField("max_connections") maxConnections: Int,
  @jsonField("drop_pending_updates") dropPendingUpdates: Boolean,
  @jsonField("allowed_updates") allowedUpdates: Option[List[String]]
) derives JsonCodec, Schema
