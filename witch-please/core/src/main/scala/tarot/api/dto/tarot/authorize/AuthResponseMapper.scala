package tarot.api.dto.tarot.authorize

import shared.api.dto.tarot.authorize.AuthResponse
import tarot.domain.models.authorize.Token

object AuthResponseMapper {
  def fromDomain(token: Token): AuthResponse =
    AuthResponse(
      token = token.token,
      role = token.role
    )
}
