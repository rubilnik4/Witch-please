package tarot.infrastructure.repositories.spreads

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.*
import tarot.infrastructure.repositories.cards.CardDao
import tarot.infrastructure.repositories.photo.PhotoDao
import zio.{ZIO, *}

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class SpreadRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends SpreadRepository {
  private val spreadDao = SpreadDao(quill)
  private val cardDao = CardDao(quill)
  private val photoDao = PhotoDao(quill)

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

  override def getScheduleSpreads(deadline: Instant, limit: Int): ZIO[Any, TarotError, List[Spread]] =
    for {
      _ <- ZIO.logDebug(s"Getting scheduled spreads by deadline $deadline")

      spreads <- spreadDao.getScheduledSpreads(deadline, limit)
        .tapError(e => ZIO.logErrorCause(s"Failed to get ready spreads by deadline $deadline", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get ready spreads by deadline $deadline", e))
        .flatMap(spreads => ZIO.foreach(spreads)(SpreadPhotoEntity.toDomain))
    } yield spreads

  override def getPreviewSpreads(deadline: Instant, limit: Int): ZIO[Any, TarotError, List[Spread]] =
    for {
      _ <- ZIO.logDebug(s"Getting preview spreads by deadline $deadline")

      spreads <- spreadDao.getPreviewSpreads(deadline, limit)
        .tapError(e => ZIO.logErrorCause(s"Failed to get preview spreads by deadline $deadline", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get preview spreads by deadline $deadline", e))
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
      _ <- ZIO.logDebug(s"Creating spread $spread")

      spreadId <- quill.transaction {
        for {
          photoId <- photoDao.insertPhoto(PhotoEntity.toEntity(spread.photo))
          spreadEntity = SpreadEntity.toEntity(spread, photoId)
          spreadId <- spreadDao.insertSpread(spreadEntity)
        } yield spreadId
      }
        .tapError(e => ZIO.logErrorCause(s"Failed to create spread $spread", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to create spread ${spread.id}", e.getCause))
    } yield SpreadId(spreadId)

  override def updateSpreadStatus(spreadStatusUpdate: SpreadStatusUpdate): ZIO[Any, TarotError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Updating spread status $spreadStatusUpdate")

      result <- (spreadStatusUpdate match {
        case SpreadStatusUpdate.Scheduled(spreadId, scheduledAt, cardOfDayAt, expectedAt) =>
          spreadDao.updateToSchedule(spreadId.id, scheduledAt, cardOfDayAt, expectedAt)
        case SpreadStatusUpdate.PreviewPublished(spreadId) =>
          spreadDao.updateToPreviewPublish(spreadId.id)
        case SpreadStatusUpdate.Published(spreadId, publishedAt) =>
          spreadDao.updateToPublish(spreadId.id, publishedAt)
      })
        .tapError(e => ZIO.logErrorCause(s"Failed to update spread status $spreadStatusUpdate", Cause.fail(e)))
        .mapError(e => DatabaseError("Failed to update spread status", e))

      _ <- result match {
        case 0L => ZIO.fail(TarotError.Conflict(s"Spread state conflict: $spreadStatusUpdate")) *>
          ZIO.logError(s"Spread state conflict: $spreadStatusUpdate")
        case _ => ZIO.unit
      }
    } yield ()

  override def updateSpread(spreadId: SpreadId, spread: SpreadUpdate): ZIO[Any, TarotError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Updating spread $spreadId")

      spreadId <- quill.transaction {
        for {
          photoId <- photoDao.insertPhoto(PhotoEntity.toEntity(spread.photo))         
          spreadId <- spreadDao.updateSpread(spreadId.id, spread, photoId)
        } yield spreadId
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
