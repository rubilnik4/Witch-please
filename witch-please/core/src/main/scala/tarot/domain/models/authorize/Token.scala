package tarot.domain.models.authorize

import shared.models.tarot.authorize.Role

case class Token(
  token: String,
  role: Role
)
