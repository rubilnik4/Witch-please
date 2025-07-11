package tarot.api.dto.tarot.auth

import tarot.domain.models.auth.{ClientType, Role}
import zio.json.*
import zio.schema.*

final case class TokenPayload(clientType: ClientType, projectId: String, role: Role)
  derives JsonCodec, Schema
