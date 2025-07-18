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

  def getByClientId(clientId: String): ZIO[Any, SQLException, Option[UserEntity]] =
    run(
      quote {
        userTable
          .filter(user => user.clientId == lift(clientId))
          .take(1)
      }
    ).map(_.headOption)

  def existsByClientId(clientId: String): ZIO[Any, SQLException, Boolean] =
    run(
      quote {
        userTable
          .filter { user => user.clientId == lift(clientId) }
          .take(1)
          .nonEmpty
      })

  private inline def userTable =
    quote(querySchema[UserEntity](TarotTableNames.users))

}
