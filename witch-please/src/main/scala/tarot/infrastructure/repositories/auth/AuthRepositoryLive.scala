package tarot.infrastructure.repositories.auth

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.auth.{User, UserId, UserProject, UserRole}
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.photo.Photo
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatusUpdate}
import tarot.infrastructure.repositories.TarotRepository
import tarot.layers.AppEnv
import zio.*

import java.sql.SQLException
import java.util.UUID

final class AuthRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends AuthRepository {
  import quill.*

  private val authDao = AuthDao(quill)

  def getUserProject(userId: UserId, projectId: ProjectId): ZIO[AppEnv, TarotError, Option[UserProject]] =
    authDao
      .getUserProject(userId.id, projectId.id)
      .mapError(e => DatabaseError(s"Failed to get userProject $userId for project $projectId", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to get userProject $userId for project $projectId from database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully get userProject $userId for project $projectId from database")
      ).flatMap {
        case Some(userProject) =>
          ZIO.some(UserProjectEntity.toDomain(userProject))
        case None =>
          ZIO.none
      }

  def getUserRole(userId: UserId, projectId: ProjectId): ZIO[AppEnv, TarotError, Option[UserRole]] =
    authDao
      .getUserRole(userId.id, projectId.id)
      .mapError(e => DatabaseError(s"Failed to get userRole $userId for project $projectId", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to get userRole $userId for project $projectId from database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully get userRole $userId for project $projectId from database")
      ).flatMap {
        case Some(userRole) =>
          ZIO.some(UserRoleEntity.toDomain(userRole))
        case None =>
          ZIO.none
      }
}
