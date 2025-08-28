package shared.api.dto.tarot.authorize

import shared.models.tarot.authorize.ClientType
import zio.json.*
import sttp.tapir.Schema

import java.util.UUID

final case class AuthRequest(
  clientType: ClientType,
  userId: UUID,
  clientSecret: String,
  projectId: Option[UUID]
) derives JsonCodec, Schema
