package tarot.infrastructure.repositories.users

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.*
import tarot.infrastructure.repositories.TarotTableNames
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class UserDao(quill: Quill.Postgres[SnakeCase]) {
  import UserQuillMappings.given
  import quill.*

  def insertUser(user: UserEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        userTable
          .insertValue(lift(user))
          .returning(_.id)
      })

  def getUser(userId: UUID): ZIO[Any, SQLException, Option[UserEntity]] =
    run(
      quote {
        userTable
          .filter(userEntity => userEntity.id == lift(userId))
          .take(1)
      }
    ).map(_.headOption)

  def getUserByClientId(clientId: String): ZIO[Any, SQLException, Option[UserEntity]] =
    run(
      quote {
        userTable
          .filter(userEntity => userEntity.clientId == lift(clientId))
          .take(1)
      }
    ).map(_.headOption)

  def existsUser(userId: UUID): ZIO[Any, SQLException, Boolean] =
    run(
      quote {
        userTable
          .filter { userEntity => userEntity.id == lift(userId) }
          .take(1)
          .nonEmpty
      })

  def existsUserByClientId(clientId: String): ZIO[Any, SQLException, Boolean] =
    run(
      quote {
        userTable
          .filter { userEntity => userEntity.clientId == lift(clientId) }
          .take(1)
          .nonEmpty
      })

  private inline def userTable =
    quote(querySchema[UserEntity](TarotTableNames.users))
}
