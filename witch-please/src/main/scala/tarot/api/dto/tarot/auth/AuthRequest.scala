package tarot.api.dto.tarot.auth

import tarot.domain.models.auth.ClientType
import zio.json.*
import zio.schema.*

final case class AuthRequest(
  clientType: ClientType,
  project: String,
  clientSecret: String
) derives JsonCodec, Schema
