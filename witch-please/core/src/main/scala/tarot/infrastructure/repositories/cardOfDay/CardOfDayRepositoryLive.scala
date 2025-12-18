package tarot.infrastructure.repositories.cardOfDay

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.cardOfDay.{CardOfDay, CardOfDayId, CardOfDayStatusUpdate}
import tarot.domain.models.cards.{Card, CardId, CardUpdate}
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatusUpdate}
import tarot.infrastructure.repositories.photo.PhotoDao
import zio.*

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class CardOfDayRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends CardOfDayRepository {
  private val cardOfDayDao = CardOfDayDao(quill)
  private val photoDao = PhotoDao(quill)

  override def getCardOfDay(spreadId: SpreadId): ZIO[Any, TarotError, Option[CardOfDay]] =
    for {
      _ <- ZIO.logDebug(s"Getting card of day by spreadId $spreadId")

      cardOfDay <- cardOfDayDao.getCardOfDay(spreadId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get card of day by spreadId $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError("Failed to get card of day by spreadId", e))
        .flatMap(spreadMaybe => ZIO.foreach(spreadMaybe)(CardOfDayPhotoEntity.toDomain))
    } yield cardOfDay
    
  override def getScheduledCardsOfDay(deadline: Instant, limit: Int): ZIO[Any, TarotError, List[CardOfDay]] =
    for {
      _ <- ZIO.logDebug(s"Getting scheduled cards of day by deadline $deadline")

      cardsOfDay <- cardOfDayDao.getScheduledCardsOfDay(deadline, limit)
        .tapError(e => ZIO.logErrorCause(s"Failed to get scheduled cards of day by deadline $deadline", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get scheduled cards of day by deadline $deadline", e))
        .flatMap(cardsOfDay => ZIO.foreach(cardsOfDay)(CardOfDayPhotoEntity.toDomain))
    } yield cardsOfDay

  override def existCardOfDay(spreadId: SpreadId): ZIO[Any, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Checking exist card of day by spread $spreadId")

      exist <- cardOfDayDao.existCardOfDay(spreadId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to check exist card of day by spread $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to check exist card of day by spread $spreadId", e.getCause))
    } yield exist
    
  override def createCardOfDay(cardOfDay: CardOfDay): ZIO[Any, TarotError, CardOfDayId] =
    for {
      _ <- ZIO.logDebug(s"Creating card of day ${cardOfDay.id}")

      cardOfDayId <- quill.transaction {
        for {
          photoId <- photoDao.insertPhoto(PhotoEntity.toEntity(cardOfDay.photo))
          cardOfDayEntity = CardOfDayEntity.toEntity(cardOfDay, photoId)
          cardOfDayId <- cardOfDayDao.insertCard(cardOfDayEntity)
        } yield cardOfDayId
      }
        .tapError(e => ZIO.logErrorCause(s"Failed to create card of day ${cardOfDay.id}", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to create card of day ${cardOfDay.id}", e.getCause))
    } yield CardOfDayId(cardOfDayId)

  override def updateCardOfDayStatus(cardOfDayStatusUpdate: CardOfDayStatusUpdate): ZIO[Any, TarotError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Updating card of day status $cardOfDayStatusUpdate")

      result <- (cardOfDayStatusUpdate match {
        case CardOfDayStatusUpdate.Scheduled(cardOfDayId, scheduledAt, expectedAt) =>
          cardOfDayDao.updateToSchedule(cardOfDayId.id, scheduledAt, expectedAt)
        case CardOfDayStatusUpdate.Published(cardOfDayId, publishedAt) =>
          cardOfDayDao.updateToPublish(cardOfDayId.id, publishedAt)
      })
        .tapError(e => ZIO.logErrorCause(s"Failed to update card of day status $cardOfDayStatusUpdate", Cause.fail(e)))
        .mapError(e => DatabaseError("Failed to update card of day status", e))

      _ <- result match {
        case 0L => ZIO.fail(TarotError.Conflict(s"Card of day state conflict: $cardOfDayStatusUpdate")) *>
          ZIO.logError(s"Card of day state conflict: $cardOfDayStatusUpdate")
        case _ => ZIO.unit
      }
    } yield ()  
}
