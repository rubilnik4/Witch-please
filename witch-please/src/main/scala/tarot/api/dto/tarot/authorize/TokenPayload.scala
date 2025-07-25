package tarot.api.dto.tarot.authorize

import tarot.domain.models.auth.{ClientType, Role}
import zio.json.*
import zio.schema.*

import java.util.UUID

final case class TokenPayload(
  clientType: ClientType,
  userId: UUID,
  projectId: Option[UUID],
  role: Role
) derives JsonCodec, Schema
