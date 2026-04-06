package tarot.infrastructure.repositories.spreads

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.*
import tarot.infrastructure.repositories.cardsOfDay.CardOfDayDao
import tarot.infrastructure.repositories.cards.CardDao
import tarot.infrastructure.repositories.photo.{PhotoDao, PhotoObjectDao}
import zio.*

import java.time.Instant

final class SpreadRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends SpreadRepository {
  private val spreadDao = SpreadDao(quill)
  private val cardDao = CardDao(quill)
  private val cardOfDayDao = CardOfDayDao(quill)
  private val photoDao = PhotoDao(quill)
  private val photoObjectDao = PhotoObjectDao(quill)

  override def getSpread(spreadId: SpreadId): ZIO[Any, TarotError, Option[Spread]] =
    for {
      _ <- ZIO.logDebug(s"Getting spread $spreadId")

      spread <- spreadDao.getSpread(spreadId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get spread $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError("Failed to get spread", e))
        .flatMap(spreadMaybe => ZIO.foreach(spreadMaybe)(SpreadPhotoEntity.toDomain))
    } yield spread

  override def getSpreads(projectId: ProjectId): ZIO[Any, TarotError, List[Spread]] =
    for {
      _ <- ZIO.logDebug(s"Getting spread by projectId $projectId")

      spreads <- spreadDao.getSpreads(projectId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get spreads by projectId $projectId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get spreads by projectId $projectId", e))
        .flatMap(spreads => ZIO.foreach(spreads)(SpreadPhotoEntity.toDomain))
    } yield spreads

  override def getScheduledSpreads(deadline: Instant, limit: Int): ZIO[Any, TarotError, List[Spread]] =
    for {
      _ <- ZIO.logDebug(s"Getting scheduled spreads by deadline $deadline")

      spreads <- spreadDao.getScheduledSpreads(deadline, limit)
        .tapError(e => ZIO.logErrorCause(s"Failed to get scheduled spreads by deadline $deadline", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get scheduled spreads by deadline $deadline", e))
        .flatMap(spreads => ZIO.foreach(spreads)(SpreadPhotoEntity.toDomain))
    } yield spreads

  override def existsSpread(spreadId: SpreadId): ZIO[Any, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Checking spread $spreadId")

      exists <- spreadDao.existsSpread(spreadId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to check spread $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to check spread $spreadId", e))
    } yield exists

  override def createSpread(spread: Spread): ZIO[Any, TarotError, SpreadId] =
    for {
      _ <- ZIO.logDebug(s"Creating spread ${spread.id}")

      spreadId <- quill.transaction {
        for {
          photoObjectId <- photoObjectDao.findOrCreatePhotoObjectId(spread.photo.photoObject)
          photoId <- photoDao.insertPhoto(PhotoEntity.toEntity(spread.photo, photoObjectId))
          spreadEntity = SpreadEntity.toEntity(spread)
          spreadId <- spreadDao.insertSpread(spreadEntity)
        } yield spreadId
      }
        .tapError(e => ZIO.logErrorCause(s"Failed to create spread ${spread.id}", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to create spread ${spread.id}", e.getCause))
    } yield SpreadId(spreadId)

  override def scheduleSpread(spreadId: SpreadId, scheduledAt: Instant, cardOfDayId: tarot.domain.models.cardsOfDay.CardOfDayId, cardOfDayAt: Instant): ZIO[Any, TarotError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Scheduling spread $spreadId with card of day $cardOfDayId")

      scheduleResult <- quill.transaction {
        for {
          spreadRows <- spreadDao.updateToSchedule(spreadId.id, scheduledAt)
          cardOfDayRows <-
            if (spreadRows == 0L) ZIO.succeed((spreadRows, 0L))
            else cardOfDayDao.updateToSchedule(cardOfDayId.id, cardOfDayAt).map(rows => (spreadRows, rows))
        } yield cardOfDayRows
      }
        .tapError(e => ZIO.logErrorCause(s"Failed to schedule spread $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to schedule spread $spreadId", e))

      (spreadRows, cardOfDayRows) = scheduleResult
      _ <- ZIO.when(spreadRows == 0L) {
        ZIO.logError(s"Spread state conflict while scheduling: $spreadId") *>
          ZIO.fail(TarotError.Conflict(s"Spread state conflict while scheduling: $spreadId"))
      }
      _ <- ZIO.when(cardOfDayRows == 0L) {
        ZIO.logError(s"Card of day state conflict while scheduling: $cardOfDayId") *>
          ZIO.fail(TarotError.Conflict(s"Card of day state conflict while scheduling: $cardOfDayId"))
      }
    } yield ()

  override def updateSpreadStatus(spreadStatusUpdate: SpreadStatusUpdate): ZIO[Any, TarotError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Updating spread status $spreadStatusUpdate")

      spreadRows <- updateStatus(spreadStatusUpdate)
        .tapError(e => ZIO.logErrorCause(s"Failed to update spread status $spreadStatusUpdate", Cause.fail(e)))
        .mapError(e => DatabaseError("Failed to update spread status", e))

      _ <- spreadRows match {
        case 0L =>
          ZIO.logError(s"Spread state conflict: $spreadStatusUpdate") *>
            ZIO.fail(TarotError.Conflict(s"Spread state conflict: $spreadStatusUpdate"))
        case _ => ZIO.unit
      }
    } yield ()

  private def updateStatus(spreadStatusUpdate: SpreadStatusUpdate) =
    spreadStatusUpdate match {
      case SpreadStatusUpdate.Error(spreadId) =>
        spreadDao.updateToError(spreadId.id)
      case SpreadStatusUpdate.Published(spreadId, publishedAt) =>
        spreadDao.updateToPublish(spreadId.id, publishedAt)
    }

  override def updateSpread(spreadId: SpreadId, spread: SpreadUpdate): ZIO[Any, TarotError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Updating spread $spreadId")

      _ <- quill.transaction {
        for {
          photoObjectId <- photoObjectDao.findOrCreatePhotoObjectId(spread.photo.photoObject)
          photoId <- photoDao.insertPhoto(PhotoEntity.toEntity(spread.photo, photoObjectId))
          _ <- spreadDao.updateSpread(spreadId.id, spread, photoId)
        } yield ()
      }
        .tapError(e => ZIO.logErrorCause(s"Failed to update spread $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to update spread $spreadId", e))
    } yield ()

  override def deleteSpread(spreadId: SpreadId): ZIO[Any, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Deleting spread $spreadId")

      count <-  spreadDao.deleteSpread(spreadId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to delete spread $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to delete spread $spreadId", e))
    } yield count > 0
}
