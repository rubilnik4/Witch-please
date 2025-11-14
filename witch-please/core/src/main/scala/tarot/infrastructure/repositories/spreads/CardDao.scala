package tarot.infrastructure.repositories.spreads

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.{CardEntity, CardPhotoEntity, PhotoEntity}
import tarot.infrastructure.repositories.TarotTableNames
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class CardDao(quill: Quill.Postgres[SnakeCase]) {
  import SpreadQuillMappings.given
  import quill.*

  def getCards(spreadId: UUID): ZIO[Any, SQLException, List[CardPhotoEntity]] =
    run(
      quote {
        cardTable
          .join(photoTable)
          .on((card, photo) => card.photoId == photo.id)
          .filter { case (card, _) => card.spreadId == lift(spreadId) }
          .map { case (card, photo) => CardPhotoEntity(card, photo) }
      })

  def getCardsCount(spreadId: UUID): ZIO[Any, SQLException, Long] =
    run(
      quote {
        cardTable
          .filter { card => card.spreadId == lift(spreadId) }
          .size
      })

  def insertCard(card: CardEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        cardTable
          .insertValue(lift(card))
          .returning(_.id)
      })

  private inline def cardTable =
    quote(querySchema[CardEntity](TarotTableNames.cards))

  private inline def photoTable =
    quote(querySchema[PhotoEntity](TarotTableNames.photos))
}
