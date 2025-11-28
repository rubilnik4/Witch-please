package tarot.infrastructure.repositories.cards

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.photo.Photo
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatusUpdate}
import tarot.infrastructure.repositories.cards.CardDao
import tarot.infrastructure.repositories.photo.PhotoDao
import zio.*

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class CardRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends CardRepository {
  private val cardDao = CardDao(quill)
  private val photoDao = PhotoDao(quill)

  def getCards(spreadId: SpreadId): ZIO[Any, TarotError, List[Card]] =
    for {
      _ <- ZIO.logDebug(s"Getting cards by spreadId $spreadId")

      cards <- cardDao.getCards(spreadId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get cards by spreadId $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get cards by spreadId $spreadId", e))
        .flatMap(cards => ZIO.foreach(cards)(CardPhotoEntity.toDomain))
    } yield cards

  def getCardsCount(spreadId: SpreadId): ZIO[Any, TarotError, Long] =
    for {
      _ <- ZIO.logDebug(s"Getting cards count by spreadId $spreadId")

      cardsCount <- cardDao.getCardsCount(spreadId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get cards count by spreadId $spreadId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get cards count by spreadId $spreadId", e))
    } yield cardsCount
  
  def createCard(card: Card): ZIO[Any, TarotError, CardId] =
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
}
