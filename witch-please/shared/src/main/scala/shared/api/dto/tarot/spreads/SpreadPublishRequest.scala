package shared.api.dto.tarot.spreads

import zio.json.*
import zio.schema.*
import zio.{Clock, ZIO}

import java.time.Instant

final case class SpreadPublishRequest(
  scheduledAt: Instant
) derives JsonCodec, Schema