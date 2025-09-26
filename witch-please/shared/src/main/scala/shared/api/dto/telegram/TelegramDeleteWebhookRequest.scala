package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

final case class TelegramDeleteWebhookRequest(
  @jsonField("drop_pending_updates") dropPendingUpdates: Boolean
) derives JsonCodec, Schema
