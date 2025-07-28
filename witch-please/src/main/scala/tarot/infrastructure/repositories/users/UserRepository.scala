package tarot.infrastructure.repositories.users

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{User, UserId, UserProject, UserRole}
import tarot.domain.models.projects.ProjectId
import tarot.layers.AppEnv
import zio.ZIO

trait UserRepository {
  def createUser(user: User): ZIO[Any, TarotError, UserId]
  def getUser(userId: UserId): ZIO[Any, TarotError, Option[User]]
  def getUserByClientId(clientId: String): ZIO[Any, TarotError, Option[User]]
  def existsUser(userId: UserId): ZIO[Any, TarotError, Boolean]
  def existsUserByClientId(clientId: String): ZIO[Any, TarotError, Boolean]
}
