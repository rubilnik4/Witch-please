package tarot.domain.models.auth

import zio.json.*

enum Role:
  case PreProject, Admin, User

object Role {
  given JsonCodec[Role] = DeriveJsonCodec.gen[Role]
}