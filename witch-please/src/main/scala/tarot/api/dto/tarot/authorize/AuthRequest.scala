package tarot.api.dto.tarot.authorize

import tarot.domain.models.authorize.ClientType
import zio.json.*
import zio.schema.*

import java.util.UUID

final case class AuthRequest(
  clientType: ClientType,
  userId: UUID,
  clientSecret: String,
  projectId: Option[UUID]
) derives JsonCodec, Schema
