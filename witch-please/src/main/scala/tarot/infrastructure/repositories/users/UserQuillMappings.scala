package tarot.infrastructure.repositories.users

import io.getquill.MappedEncoding
import tarot.domain.models.authorize.{ClientType, Role}

object UserQuillMappings {
  given MappedEncoding[ClientType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, ClientType] = MappedEncoding(ClientType.valueOf)
}
