package tarot.infrastructure.repositories

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.{CardEntity, PhotoEntity, SpreadEntity, SpreadPhotoEntity}
import tarot.domain.models.photo.{PhotoOwnerType, PhotoStorageType}
import tarot.domain.models.spreads.SpreadStatus
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class CardDao(quill: Quill.Postgres[SnakeCase]) {
  import QuillMappings.*
  import quill.*

  private inline def cardTable = quote {
    querySchema[CardEntity](TarotTableNames.Cards)
  }

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
}
