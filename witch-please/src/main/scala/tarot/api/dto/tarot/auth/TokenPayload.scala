package tarot.api.dto.tarot.auth

import zio.json.*
import zio.schema.*

final case class TokenPayload(sub: String, role: String)
  derives JsonCodec, Schema
