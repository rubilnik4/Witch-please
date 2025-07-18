package tarot.infrastructure.repositories.users

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

final class UserRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends UserRepository {
  private val userDao = UserDao(quill)

  def createUser(user: User): ZIO[Any, TarotError, UserId] =
    userDao
      .insertUser(UserEntity.toEntity(user))
      .mapBoth(
        e => DatabaseError(s"Failed to create user $user", e), 
        UserId(_))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to create user $user to database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully create user $user to database")
      )

  def getByClientId(clientId: String): ZIO[Any, TarotError, Option[User]] =
    userDao
      .getByClientId(clientId)
      .mapError(e => DatabaseError(s"Failed to get user by clientId $clientId", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to get user by clientId $clientId from database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully get user by clientId $clientId from database")
      ).flatMap {
        case Some(user) =>
          ZIO.some(UserEntity.toDomain(user))
        case None =>
          ZIO.none
      }

  def existsByClientId(clientId: String): ZIO[Any, TarotError, Boolean] =
    userDao
      .existsByClientId(clientId)
      .mapError(e => DatabaseError(s"Failed to check user by clientId $clientId", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to check user $clientId from database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully check user $clientId from database")
      )
}
