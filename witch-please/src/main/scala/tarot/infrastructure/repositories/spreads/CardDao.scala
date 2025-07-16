package tarot.infrastructure.repositories.spreads

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.CardEntity
import tarot.infrastructure.repositories.TarotTableNames
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class CardDao(quill: Quill.Postgres[SnakeCase]) {
  import SpreadQuillMappings.*
  import quill.*

  def insertCard(card: CardEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        cardTable
          .insertValue(lift(card))
          .returning(_.id)
      })
  
  def countCards(spreadId: UUID): ZIO[Any, SQLException, Long] =
    run(
      quote {
        cardTable
          .filter(_.spreadId == lift(spreadId))
          .size
      }
    )

  private inline def cardTable =
    quote(querySchema[CardEntity](TarotTableNames.cards))
}
