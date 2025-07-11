package tarot.domain.models.auth

import zio.json.*
import zio.schema.*

enum Role:
  case Admin, User

object Role {
  given JsonCodec[Role] = DeriveJsonCodec.gen[Role]
}