package tarot.infrastructure.services.users

import tarot.api.dto.tarot.auth.TokenPayload
import tarot.domain.models.TarotError
import tarot.domain.models.auth.{ClientType, ExternalUser, UserId}
import tarot.domain.models.projects.ProjectId
import tarot.layers.AppEnv
import zio.ZIO

trait UserService {
  def createUser(externalUser: ExternalUser): ZIO[AppEnv, TarotError, UserId]
}
