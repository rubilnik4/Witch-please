package tarot.infrastructure.repositories.auth

import io.getquill.MappedEncoding
import tarot.domain.models.auth.{ClientType, Role}

object AuthQuillMappings {
  given MappedEncoding[Role, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, Role] = MappedEncoding(Role.valueOf)

  given MappedEncoding[ClientType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, ClientType] = MappedEncoding(ClientType.valueOf)
}
