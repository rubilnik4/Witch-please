package tarot.infrastructure.repositories.channels

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.*
import tarot.domain.models.channels.UserChannelUpdate
import tarot.infrastructure.repositories.TarotTableNames
import tarot.infrastructure.repositories.users.{UserProjectQuillMappings, UserQuillMappings}
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class UserChannelDao(quill: Quill.Postgres[SnakeCase]) {
  import UserProjectQuillMappings.given
  import quill.*

  def insertUserChannel(userChannel: UserChannelEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        userChannelTable
          .insertValue(lift(userChannel))
          .returning(_.id)
      })

  def updateUserChannel(userChannelId: UUID, userChannel: UserChannelUpdate): ZIO[Any, SQLException, Long] =
    run(
      quote {
        userChannelTable
          .filter(_.id == lift(userChannelId))
          .update(
            _.channelId -> lift(userChannel.channelId),
            _.name -> lift(userChannel.name)
          )
      }
    )

  def getUserChannel(userChannelId: UUID): ZIO[Any, SQLException, Option[UserChannelEntity]] =
    run(
      quote {
        userChannelTable
          .filter(userChannelEntity => userChannelEntity.id == lift(userChannelId))
          .take(1)
      }
    ).map(_.headOption)  

  def getUserChannelByUser(userId: UUID): ZIO[Any, SQLException, Option[UserChannelEntity]] =
    run(
      quote {
        userChannelTable
          .filter(userChannelEntity => userChannelEntity.userId == lift(userId))
          .take(1)
      }
    ).map(_.headOption)

  def getUserChannelByProject(projectId: UUID): ZIO[Any, SQLException, Option[UserChannelEntity]] =
    run(
      quote {
        userProjectTable
          .filter(userProject => userProject.projectId == lift(projectId))
          .join(userChannelTable).on((userProject, userChannel) => userProject.userId == userChannel.userId)
          .map { case (_, userChannel) => userChannel }
          .take(1)
      }
    ).map(_.headOption)

  def existsUserChannels(userId: UUID): ZIO[Any, SQLException, Boolean] =
    run(
      quote {
        userChannelTable
          .filter(userChannelEntity => userChannelEntity.userId == lift(userId))
          .nonEmpty
      }
    )

  private inline def userChannelTable =
    quote(querySchema[UserChannelEntity](TarotTableNames.userChannels))

  private inline def userProjectTable =
    quote(querySchema[UserProjectEntity](TarotTableNames.userProjects))
}
