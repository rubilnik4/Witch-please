package shared.api.dto.tarot.channels

import zio.json.*
import zio.schema.*

import java.time.Instant
import java.util.UUID

final case class UserChannelResponse(
  id: UUID,
  channelId: Long,
  name: String,
  createdAt: Instant
) derives JsonCodec, Schema
