package shared.api.dto.tarot.users

import shared.models.tarot.authorize.ClientType
import zio.json.*
import zio.schema.*

import java.time.Instant
import java.util.UUID

final case class UserResponse(
  id: UUID,
  clientId: String,
  clientType: ClientType,
  name: String,
  createdAt: Instant
) derives JsonCodec, Schema