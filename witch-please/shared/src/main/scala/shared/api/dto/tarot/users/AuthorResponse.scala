package shared.api.dto.tarot.users

import zio.json.*
import zio.schema.*

import java.util.UUID

final case class AuthorResponse(
  id: UUID,
  name: String,
  spreadsCount: Long
) derives JsonCodec, Schema