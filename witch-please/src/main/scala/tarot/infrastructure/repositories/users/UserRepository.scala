package tarot.infrastructure.repositories.users

import tarot.domain.models.TarotError
import tarot.domain.models.auth.{User, UserId, UserProject, UserRole}
import tarot.domain.models.projects.ProjectId
import tarot.layers.AppEnv
import zio.ZIO

trait UserRepository {
  def createUser(user: User): ZIO[Any, TarotError, UserId]
  def getByClientId(clientId: String): ZIO[Any, TarotError, Option[User]]
  def exists(userId: UserId): ZIO[Any, TarotError, Boolean]
  def existsByClientId(clientId: String): ZIO[Any, TarotError, Boolean]
}
