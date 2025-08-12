package shared.api.dto.tarot.authorize

import shared.models.tarot.authorize.*
import zio.json.*
import zio.schema.*

final case class AuthResponse(
  token: String,
  role: Role
) derives JsonCodec, Schema