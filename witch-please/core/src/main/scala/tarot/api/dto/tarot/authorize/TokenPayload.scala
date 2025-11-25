package tarot.api.dto.tarot.authorize

import shared.models.tarot.authorize.{ClientType, Role}
import zio.json._
import sttp.tapir.Schema

import java.util.UUID

final case class TokenPayload(
  clientType: ClientType,
  userId: UUID,
  role: Role
) derives JsonCodec, Schema
