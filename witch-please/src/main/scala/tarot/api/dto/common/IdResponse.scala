package tarot.api.dto.common

import zio.json.*
import zio.schema.*

import java.util.UUID

final case class IdResponse(id: UUID) derives JsonCodec, Schema
