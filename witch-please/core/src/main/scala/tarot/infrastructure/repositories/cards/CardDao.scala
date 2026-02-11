package tarot.infrastructure.repositories.cards

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.{CardEntity, CardPhotoEntity, PhotoEntity}
import tarot.domain.models.cards.CardUpdate
import tarot.infrastructure.repositories.TarotTableNames
import tarot.infrastructure.repositories.photo.PhotoQuillMappings
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class CardDao(quill: Quill.Postgres[SnakeCase]) {
  import PhotoQuillMappings.given
  import quill.*

  def getCard(cardId: UUID): ZIO[Any, SQLException, Option[CardPhotoEntity]] =
    run(
      quote {
        cardTable
          .join(photoTable)
          .on((card, photo) => card.photoId == photo.id)
          .filter { case (card, _) => card.id == lift(cardId) }
          .take(1)
          .map { case (card, photo) => CardPhotoEntity(card, photo) }
      })
      .map(_.headOption)

  def getCards(spreadId: UUID): ZIO[Any, SQLException, List[CardPhotoEntity]] =
    run(
      quote {
        cardTable
          .join(photoTable)
          .on((card, photo) => card.photoId == photo.id)
          .filter { case (card, _) => card.spreadId == lift(spreadId) }
          .map { case (card, photo) => CardPhotoEntity(card, photo) }
      })

  def getCardIds(spreadId: UUID): ZIO[Any, SQLException, List[UUID]] =
    run(
      quote {
        cardTable
          .filter { card => card.spreadId == lift(spreadId) }
          .map { card => card.id }
      })
      
  def getCardsCount(spreadId: UUID): ZIO[Any, SQLException, Long] =
    run(
      quote {
        cardTable
          .filter { card => card.spreadId == lift(spreadId) }
          .size
      })

  def existCardPosition(spreadId: UUID, position: Int): ZIO[Any, SQLException, Boolean] =
    run(
      quote {
        cardTable
          .filter { card =>
            card.spreadId == lift(spreadId) &&
            card.position == lift(position) }
          .take(1)
          .nonEmpty
      })

  def insertCard(card: CardEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        cardTable
          .insertValue(lift(card))
          .returning(_.id)
      })

  def insertCards(cards: List[CardEntity]): ZIO[Any, SQLException, List[UUID]] =
    run(
      quote {
        liftQuery(cards).foreach { card =>
          cardTable.insertValue(card).returning(_.id)
        }
      })  

  def updateCard(cardId: UUID, card: CardUpdate, photoId: UUID): ZIO[Any, SQLException, Long] =
    run(
      quote {
        cardTable
          .filter(_.id == lift(cardId))
          .update(
            _.title -> lift(card.title),
            _.description -> lift(card.description),
            _.photoId -> lift(photoId)
          )
      }
    )

  def deleteCard(cardId: UUID): ZIO[Any, SQLException, Long] =
    run(
      quote {
        cardTable
          .filter(_.id == lift(cardId))
          .delete
      })
      
  private inline def cardTable =
    quote(querySchema[CardEntity](TarotTableNames.cards))

  private inline def photoTable =
    quote(querySchema[PhotoEntity](TarotTableNames.photos))
}
