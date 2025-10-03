package shared.api.dto.tarot.projects

import zio.json.*
import zio.schema.*

import java.time.Instant
import java.util.UUID

final case class ProjectResponse(
  id: UUID,
  name: String,
  createdAt: Instant,
) derives JsonCodec, Schema