package shared.api.dto.tarot.common

import zio.json.*
import zio.schema.*

import java.util.UUID

final case class IdResponse(id: UUID) derives JsonCodec, Schema
