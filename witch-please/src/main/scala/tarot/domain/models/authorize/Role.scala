package tarot.domain.models.authorize

import zio.json.*

enum Role:
  case PreProject, Admin, User

object Role {
  given JsonCodec[Role] = DeriveJsonCodec.gen[Role]
}