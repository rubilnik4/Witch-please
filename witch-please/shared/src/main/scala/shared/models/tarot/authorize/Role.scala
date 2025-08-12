package shared.models.tarot.authorize

import zio.json.*

enum Role:
  case PreProject, Admin, User

object Role {
  given JsonCodec[Role] = DeriveJsonCodec.gen[Role]
}