package tarot.infrastructure.repositories.auth

import tarot.domain.models.TarotError
import tarot.domain.models.auth.{User, UserId, UserProject, UserRole}
import tarot.domain.models.projects.ProjectId
import tarot.layers.AppEnv
import zio.ZIO

trait AuthRepository {
  def getUserProject(userId: UserId, projectId: ProjectId): ZIO[AppEnv, TarotError, Option[UserProject]]
  def getUserRole(userId: UserId, projectId: ProjectId): ZIO[AppEnv, TarotError, Option[UserRole]]
}
