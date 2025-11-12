package tarot.infrastructure.repositories.spreads

import io.getquill.*
import io.getquill.extras.InstantOps
import io.getquill.jdbczio.*
import shared.models.tarot.spreads.SpreadStatus
import tarot.domain.entities.{CardEntity, PhotoEntity, SpreadEntity, SpreadPhotoEntity}
import tarot.domain.models.TarotError
import tarot.domain.models.spreads.{Spread, SpreadStatusUpdate}
import tarot.infrastructure.repositories.TarotTableNames
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class SpreadDao(quill: Quill.Postgres[SnakeCase]) {
  import SpreadQuillMappings.given
  import quill.*

  def getSpread(spreadId: UUID): ZIO[Any, SQLException, Option[SpreadPhotoEntity]] =
    run(
      quote {
        spreadTable
          .join(photoTable)
          .on((spread, photo) => spread.photoId == photo.id)
          .filter { case (spread, _) => spread.id == lift(spreadId) }
          .take(1)
          .map { case (spread, photo) => SpreadPhotoEntity(spread, photo) }
      })
      .map(_.headOption)

  def getSpreads(projectId: UUID): ZIO[Any, SQLException, List[SpreadPhotoEntity]] =
    run(
      quote {
        spreadTable
          .join(photoTable)
          .on((spread, photo) => spread.photoId == photo.id)
          .filter { case (spread, _) => spread.projectId == lift(projectId) }
          .map { case (spread, photo) => SpreadPhotoEntity(spread, photo) }
      })

  def getReadySpreads(deadline: Instant, from: Option[Instant], limit: Int): ZIO[Any, SQLException, List[SpreadPhotoEntity]] =
    run(
      quote {
        spreadTable
          .join(photoTable)
          .on((spread, photo) => spread.photoId == photo.id)
          .filter { case (spread, _) =>
            spread.spreadStatus == lift(SpreadStatus.Scheduled) &&
            spread.scheduledAt.exists(_ <= lift(deadline)) &&
            lift(from).forall(f => spread.scheduledAt.exists(_ >= f))
          }
          .sortBy { case (spread, _) => spread.scheduledAt }(Ord.asc)
          .take(lift(limit))
          .map { case (spread, photo) => SpreadPhotoEntity(spread, photo) }
      })

  def existsSpread(spreadId: UUID): ZIO[Any, SQLException, Boolean] =
    run(
      quote {
        spreadTable
          .filter { spread => spread.id == lift(spreadId) }
          .take(1)
          .nonEmpty
      })

  def validateSpread(spreadId: UUID): ZIO[Any, SQLException, Boolean] =
    run(
      quote {
        spreadTable
          .filter(_.id == lift(spreadId))
          .join(cardTable)
          .on((spread, card) => spread.id == card.spreadId)
          .groupBy(_._1)
          .map { case (spread, grouped) => (spread.id, spread.cardCount, grouped.size)}
          .filter { case (_, expected, actual) => expected == actual }
          .take(1)
          .nonEmpty
    })

  def insertSpread(spread: SpreadEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        spreadTable
          .insertValue(lift(spread))
          .returning(_.id)
      })

  def updateToSchedule(spreadId: UUID, scheduleAt: Instant, expectedAt: Option[Instant]): ZIO[Any, SQLException, Long] =
    expectedAt match {
      case Some(expected) =>
        run(quote {
          spreadTable
            .filter(spread =>
              spread.id == lift(spreadId) && isScheduleStatus(spread) &&
              spread.scheduledAt.contains(lift(expected)))
            .update(
              _.spreadStatus -> lift(SpreadStatus.Scheduled),
              _.scheduledAt -> lift(Option(scheduleAt))
            )
        })
      case None =>
        run(quote {
          spreadTable
            .filter(spread => spread.id == lift(spreadId) && isScheduleStatus(spread))
            .update(
              _.spreadStatus -> lift(SpreadStatus.Scheduled),
              _.scheduledAt -> lift(Option(scheduleAt))
            )
        })
    }

  def updateToPublish(spreadId: UUID, publishedAt: Instant): ZIO[Any, SQLException, Long] =
    run(quote {
      spreadTable
        .filter(spread => spread.id == lift(spreadId) && isPublishStatus(spread))
        .update(
          _.spreadStatus -> lift(SpreadStatus.Published),
          _.publishedAt -> lift(Option(publishedAt))
        )
    })

  def deleteSpread(spreadId: UUID): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        spreadTable
          .filter(_.id == lift(spreadId))
          .delete
          .returning(_.id)
      })

  private inline def isScheduleStatus(spread: SpreadEntity) =
    quote(spread.spreadStatus == lift(SpreadStatus.Draft) || spread.spreadStatus == lift(SpreadStatus.Scheduled))

  private inline def isPublishStatus(spread: SpreadEntity) =
    quote(spread.spreadStatus == lift(SpreadStatus.Scheduled) && spread.publishedAt.isEmpty)
    
  private inline def spreadTable =
    quote(querySchema[SpreadEntity](TarotTableNames.spreads))

  private inline def cardTable =
    quote(querySchema[CardEntity](TarotTableNames.cards))

  private inline def photoTable =
    quote(querySchema[PhotoEntity](TarotTableNames.photos))
}
