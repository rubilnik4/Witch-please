package tarot.infrastructure.repositories.users

import io.getquill.MappedEncoding
import tarot.domain.models.auth.{ClientType, Role}

object UserProjectQuillMappings {
  given MappedEncoding[Role, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, Role] = MappedEncoding(Role.valueOf)
}
