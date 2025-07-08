package tarot.infrastructure.repositories.auth

import tarot.domain.models.TarotError
import tarot.domain.models.auth.User
import tarot.layers.AppEnv
import zio.ZIO

trait AuthRepository {
  def getByUserId(userId: UserId, projectId: ProjectId): ZIO[AppEnv, TarotError, Option[User]]
}
