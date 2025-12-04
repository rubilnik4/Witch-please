package tarot.infrastructure.repositories.cards

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.cards.{Card, CardId, CardUpdate}
import tarot.domain.models.spreads.SpreadId
import tarot.infrastructure.repositories.photo.PhotoDao
import zio.*

import java.sql.SQLException
import java.util.UUID

final class CardRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends CardRepository {
  private val cardDao = CardDao(quill)
  private val photoDao = PhotoDao(quill)

  override def getCard(cardId: CardId): ZIO[Any, TarotError, Option[Card]] =
    for {
      _ <- ZIO.logDebug(s"Getting card by cardId $cardId")

      cards <- cardDao.getCard(cardId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get card by cardId $cardId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get card by cardId $cardId", e))
        .flatMap(cards => ZIO.foreach(cards)(CardPhotoEntity.toDomain))
    } yield cards
    
  override def getCards(spreadId: SpreadId): ZIO[Any, TarotError, List[Card]] =
    for {
      _ <- ZIO.logDebug(s"Getting cards by spreadId $spreadId")

      cards <- cardDao.getCards(spreadId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get cards by spreadId $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get cards by spreadId $spreadId", e))
        .flatMap(cards => ZIO.foreach(cards)(CardPhotoEntity.toDomain))
    } yield cards

  override def getCardIds(spreadId: SpreadId): ZIO[Any, TarotError, List[CardId]]=
    for {
      _ <- ZIO.logDebug(s"Getting card ids by spreadId $spreadId")

      ids <- cardDao.getCardIds(spreadId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get card ids by spreadId $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get card ids by spreadId $spreadId", e))
    } yield ids.map(CardId(_))

  override def getCardsCount(spreadId: SpreadId): ZIO[Any, TarotError, Long] =
    for {
      _ <- ZIO.logDebug(s"Getting cards count by spreadId $spreadId")

      cardsCount <- cardDao.getCardsCount(spreadId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get cards count by spreadId $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get cards count by spreadId $spreadId", e))
    } yield cardsCount

  override def existCardPosition(spreadId: SpreadId, position: Int): ZIO[Any, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Checking card position $position exist by spreadId $spreadId")

      cardsCount <- cardDao.existCardPosition(spreadId.id, position)
        .tapError(e => ZIO.logErrorCause(s"Failed to check card position $position by spreadId $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to check card position $position by spreadId $spreadId", e))
    } yield cardsCount
    
  override def createCard(card: Card): ZIO[Any, TarotError, CardId] =
    for {
      _ <- ZIO.logDebug(s"Creating card $card")

      cardId <- quill.transaction {
        for {
          photoId <- photoDao.insertPhoto(PhotoEntity.toEntity(card.photo))
          cardEntity = CardEntity.toEntity(card, photoId)
          cardId <- cardDao.insertCard(cardEntity)
        } yield cardId
      }
        .tapError(e => ZIO.logErrorCause(s"Failed to create card $card", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to create card ${card.id}", e.getCause))
    } yield CardId(cardId)

  override def updateCard(cardId: CardId, card: CardUpdate): ZIO[Any, TarotError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Updating card $cardId")

      _ <- quill.transaction {
          for {
            photoId <- photoDao.insertPhoto(PhotoEntity.toEntity(card.photo))
            _ <- cardDao.updateSpread(cardId.id, card, photoId)
          } yield ()
        }
        .tapError(e => ZIO.logErrorCause(s"Failed to update card $cardId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to update card $cardId", e))
    } yield ()

  override def deleteCard(cardId: CardId): ZIO[Any, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Deleting card $cardId")

      count <- cardDao.deleteCard(cardId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to delete card $cardId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to delete card $cardId", e))
    } yield count > 0
}
