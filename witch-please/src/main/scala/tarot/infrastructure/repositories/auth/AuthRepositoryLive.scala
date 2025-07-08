package tarot.infrastructure.repositories.auth

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.auth.User
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.photo.Photo
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatusUpdate}
import tarot.infrastructure.repositories.TarotRepository
import tarot.layers.AppEnv
import zio.*

import java.sql.SQLException
import java.util.UUID

final class AuthRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends AuthRepository {
  import quill.*

  private val authDao = AuthDao(quill)

  def getByUserProjectId(userId: UserId, projectId: ProjectId): ZIO[AppEnv, TarotError, Option[UserProject]] =
    authDao
      .getByUserProjectId(userId.id)
      .mapError(e => DatabaseError(s"Failed to get user $userId for project $projectId", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to get user $userId for project $projectId from database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully get user $userId for project $projectId from database")
      ).flatMap {
        case Some(user) =>
          UserMapper.toDomain(user).map(Some(_))
        case None =>
          ZIO.none
      }


}
