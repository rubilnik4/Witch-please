package shared.api.dto.tarot.spreads

import zio.json.*
import zio.schema.*

import java.time.*

final case class SpreadPublishRequest(
  scheduledAt: Instant,
  cardOfDayDelayHours: Duration
) derives JsonCodec, Schema