package tarot.infrastructure.repositories.users

import shared.models.tarot.authorize.{ClientType, Role}
import io.getquill.MappedEncoding

object UserQuillMappings {
  given MappedEncoding[ClientType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, ClientType] = MappedEncoding(ClientType.valueOf)
}
