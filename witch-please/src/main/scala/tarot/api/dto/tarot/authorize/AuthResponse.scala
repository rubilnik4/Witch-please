package tarot.api.dto.tarot.authorize

import tarot.domain.models.authorize.{Role, Token}
import zio.json.*
import zio.schema.*

final case class AuthResponse(
  token: String,
  role: Role
) derives JsonCodec, Schema

object AuthResponse {
  def fromDomain(token: Token): AuthResponse =
    AuthResponse(
      token = token.token,
      role = token.role
    )
}