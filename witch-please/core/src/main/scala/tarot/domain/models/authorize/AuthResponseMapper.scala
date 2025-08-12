package tarot.domain.models.authorize

import shared.api.dto.tarot.authorize.AuthResponse

object AuthResponseMapper {
  def fromDomain(token: Token): AuthResponse =
    AuthResponse(
      token = token.token,
      role = token.role
    )
}
