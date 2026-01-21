package tarot.infrastructure.repositories.channels

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.*
import tarot.infrastructure.repositories.TarotTableNames
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class UserChannelDao(quill: Quill.Postgres[SnakeCase]) {
  import quill.*

  def insertUserChannel(userChannel: UserChannelEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        userChannelTable
          .insertValue(lift(userChannel))
          .returning(_.id)
      })

  def getUserChannel(userId: UUID): ZIO[Any, SQLException, Option[UserChannelEntity]] =
    run(
      quote {
        userChannelTable
          .filter(userChannelEntity => userChannelEntity.userId == lift(userId))
          .take(1)
      }
    ).map(_.headOption)

  private inline def userChannelTable =
    quote(querySchema[UserChannelEntity](TarotTableNames.userChannels))
}
