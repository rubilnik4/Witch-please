package tarot.infrastructure.repositories.users

import shared.models.tarot.authorize.{ClientType, Role}
import io.getquill.MappedEncoding

object UserProjectQuillMappings {
  given MappedEncoding[Role, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, Role] = MappedEncoding(Role.valueOf)
}
