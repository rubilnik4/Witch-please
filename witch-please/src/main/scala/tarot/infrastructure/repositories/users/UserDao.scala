package tarot.infrastructure.repositories.users

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.*
import tarot.domain.models.spreads.{SpreadStatus, SpreadStatusUpdate}
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

  private inline def userTable = quote {
    querySchema[UserEntity](TarotTableNames.users)
}
