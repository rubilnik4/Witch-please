package tarot.api.dto.tarot.auth

import zio.json.*
import zio.schema.*

final case class AuthRequest(clientId: String, clientSecret: String)
  derives JsonCodec, Schema
