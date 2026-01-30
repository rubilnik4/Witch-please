package shared.api.dto.tarot.channels

import sttp.tapir.Schema
import zio.json.JsonCodec

final case class ChannelUpdateRequest(
  channelId: Long,
  name: String
) extends ChannelRequest derives JsonCodec, Schema
