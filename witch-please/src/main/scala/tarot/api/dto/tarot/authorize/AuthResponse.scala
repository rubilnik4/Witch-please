package tarot.api.dto.tarot.authorize

import zio.json.*
import zio.schema.*

final case class AuthResponse(token: String)
  derives JsonCodec, Schema
