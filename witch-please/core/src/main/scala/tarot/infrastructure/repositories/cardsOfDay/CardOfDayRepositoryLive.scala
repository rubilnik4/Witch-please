package tarot.infrastructure.repositories.cardsOfDay

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.cards.CardId
import tarot.domain.models.cardsOfDay.*
import tarot.domain.models.spreads.SpreadId
import tarot.infrastructure.repositories.photo.PhotoDao
import zio.*

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class CardOfDayRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends CardOfDayRepository {
  private val cardOfDayDao = CardOfDayDao(quill)
  private val photoDao = PhotoDao(quill)

  override def getCardOfDay(cardOfDayId: CardOfDayId): ZIO[Any, TarotError, Option[CardOfDay]] =
    for {
      _ <- ZIO.logDebug(s"Getting card of day by card of day id $cardOfDayId")

      cardOfDay <- cardOfDayDao.getCardOfDay(cardOfDayId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get card of day by id $cardOfDayId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get card of day by id $cardOfDayId", e))
        .flatMap(spreadMaybe => ZIO.foreach(spreadMaybe)(CardOfDayPhotoEntity.toDomain))
    } yield cardOfDay
    
  override def getCardOfDayBySpread(spreadId: SpreadId): ZIO[Any, TarotError, Option[CardOfDay]] =
    for {
      _ <- ZIO.logDebug(s"Getting card of day by spreadId $spreadId")

      cardOfDay <- cardOfDayDao.getCardOfDayBySpread(spreadId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get card of day by spreadId $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get card of day by spreadId $spreadId", e))
        .flatMap(spreadMaybe => ZIO.foreach(spreadMaybe)(CardOfDayPhotoEntity.toDomain))
    } yield cardOfDay

  override def getCardOfDayByCard(cardId: CardId): ZIO[Any, TarotError, Option[CardOfDay]] =
    for {
      _ <- ZIO.logDebug(s"Getting card of day by cardId $cardId")

      cardOfDay <- cardOfDayDao.getCardOfDayByCard(cardId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get card of day by cardId $cardId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get card of day by cardId $cardId", e))
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
          cardOfDayEntity = CardOfDayEntity.toEntity(cardOfDay)
          cardOfDayId <- cardOfDayDao.insertCardOfDay(cardOfDayEntity)
        } yield cardOfDayId
      }
        .tapError(e => ZIO.logErrorCause(s"Failed to create card of day ${cardOfDay.id}", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to create card of day ${cardOfDay.id}", e.getCause))
    } yield CardOfDayId(cardOfDayId)

  override def updateCardOfDay(cardOfDayId: CardOfDayId, cardOfDayUpdate: CardOfDayUpdate): ZIO[Any, TarotError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Updating card ща вфн $cardOfDayId")

      _ <- quill.transaction {
          for {
            photoId <- photoDao.insertPhoto(PhotoEntity.toEntity(cardOfDayUpdate.photo))
            _ <- cardOfDayDao.updateCardOfDay(cardOfDayId.id, cardOfDayUpdate, photoId)
          } yield ()
        }
        .tapError(e => ZIO.logErrorCause(s"Failed to update card of day $cardOfDayId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to update card of day $cardOfDayId", e))
    } yield ()

  override def updateCardOfDayStatus(cardOfDayStatusUpdate: CardOfDayStatusUpdate): ZIO[Any, TarotError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Updating card of day status $cardOfDayStatusUpdate")

      result <- (cardOfDayStatusUpdate match {
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

  override def deleteCardOfDay(cardOfDayId: CardOfDayId): ZIO[Any, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Deleting card of day $cardOfDayId")

      count <- cardOfDayDao.deleteCardOfDay(cardOfDayId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to delete card of day $cardOfDayId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to delete card of day $cardOfDayId", e))
    } yield count > 0
}
