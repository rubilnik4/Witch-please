package shared.api.dto.tarot.spreads

import zio.json.*
import zio.schema.*

import java.time.Instant

final case class SpreadPublishRequest(
  scheduledAt: Instant,
  cardOfDayDelayHours: Int
) derives JsonCodec, Schema