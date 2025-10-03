package tarot.infrastructure.repositories.users

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.authorize.{User, UserId, UserProject, UserRole}
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.photo.Photo
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatusUpdate}
import tarot.infrastructure.repositories.TarotRepository
import tarot.layers.TarotEnv
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
        e => ZIO.logErrorCause(s"Failed to create user $user", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully create user $user")
      )

  def getUser(userId: UserId): ZIO[Any, TarotError, Option[User]] =
    userDao
      .getUser(userId.id)
      .mapError(e => DatabaseError(s"Failed to get user $userId", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to get user $userId", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully get user $userId")
      ).map(_.map(UserEntity.toDomain))

  def getUserByClientId(clientId: String): ZIO[Any, TarotError, Option[User]] =
    userDao
      .getUserByClientId(clientId)
      .mapError(e => DatabaseError(s"Failed to get user by clientId $clientId", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to get user by clientId $clientId", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully get user by clientId $clientId")
      ).map(_.map(UserEntity.toDomain))

  def existsUser(userId: UserId): ZIO[Any, TarotError, Boolean] =
    userDao
      .existsUser(userId.id)
      .mapError(e => DatabaseError(s"Failed to check user $userId", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to check user $userId", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully check user $userId")
      )

  def existsUserByClientId(clientId: String): ZIO[Any, TarotError, Boolean] =
    userDao
      .existsUserByClientId(clientId)
      .mapError(e => DatabaseError(s"Failed to check user by clientId $clientId", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to check user by clientId $clientId", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully check user by clientId $clientId")
      )
}
