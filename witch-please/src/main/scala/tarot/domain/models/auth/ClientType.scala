package tarot.domain.models.auth

import zio.json.*
import zio.schema.*

enum ClientType:
  case Telegram, Web, MobileApp

object ClientType {
  given JsonCodec[ClientType] = DeriveJsonCodec.gen[ClientType]
}