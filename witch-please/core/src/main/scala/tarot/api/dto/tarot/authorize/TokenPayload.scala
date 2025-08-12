package tarot.api.dto.tarot.authorize

import shared.models.tarot.authorize.{ClientType, Role}
import zio.json.*
import zio.schema.*

import java.util.UUID

final case class TokenPayload(
  clientType: ClientType,
  userId: UUID,
  projectId: Option[UUID],
  role: Role
) derives JsonCodec, Schema
