package tarot.infrastructure.repositories.users

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.authorize.{User, UserId}
import zio.*

final class UserRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends UserRepository {
  private val userDao = UserDao(quill)

  def createUser(user: User): ZIO[Any, TarotError, UserId] =
    for {
      _ <- ZIO.logDebug(s"Creating user $user")

      userId <- userDao.insertUser(UserEntity.toEntity(user))
        .tapError(e => ZIO.logErrorCause(s"Failed to create user $user", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to create user $user", e))
    } yield UserId(userId)

  def getUser(userId: UserId): ZIO[Any, TarotError, Option[User]] =
    for {
      _ <- ZIO.logDebug(s"Getting user $userId")

      userId <- userDao.getUser(userId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get user $userId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get user $userId", e))
    } yield userId.map(UserEntity.toDomain)

  def getUserByClientId(clientId: String): ZIO[Any, TarotError, Option[User]] =
    for {
      _ <- ZIO.logDebug(s"Getting user by clientId $clientId")

      userId <- userDao.getUserByClientId(clientId)
        .tapError(e => ZIO.logErrorCause(s"Failed to get user by clientId $clientId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get user by clientId $clientId", e))
    } yield userId.map(UserEntity.toDomain)

  def existsUser(userId: UserId): ZIO[Any, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Checking user $userId")

      exists <- userDao.existsUser(userId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to check user $userId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to check user $userId", e))
    } yield exists

  def existsUserByClientId(clientId: String): ZIO[Any, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Checking user by clientId $clientId")

      exists <- userDao.existsUserByClientId(clientId)
        .tapError(e => ZIO.logErrorCause(s"Failed to check user by clientId $clientId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to check user by clientId $clientId", e))
    } yield exists
}
