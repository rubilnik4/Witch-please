package shared.api.dto.tarot.channels

import sttp.tapir.Schema
import zio.json.JsonCodec

import java.time.Instant
import java.util.UUID

final case class ChannelCreateRequest(
  chatId: Long,
  name: String
) derives JsonCodec, Schema
