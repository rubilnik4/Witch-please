package tarot.infrastructure.repositories

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.{CardEntity, PhotoSourceEntity, SpreadEntity, SpreadPhotoEntity}
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

//  def getSpread(spreadId: UUID): ZIO[Any, SQLException, Option[SpreadPhotoEntity]] =
//    run(
//      quote {
//        spreadTable
//          .join(photoTable)
//          .on((spread, photo) => spread.coverPhotoId == photo.id)
//          .filter { case (spread, _) => spread.id == lift(spreadId) }
//          .take(1)
//          .map { case (spread, photo) => SpreadPhotoEntity(spread, photo) }
//      })
//      .map(_.headOption)
//
//  def existsSpread(spreadId: UUID): ZIO[Any, SQLException, Boolean] =
//    run(
//      quote {
//        spreadTable
//          .filter { spread => spread.id == lift(spreadId) }
//          .take(1)
//          .nonEmpty
//      })

  def insertCard(card: CardEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        cardTable
          .insertValue(lift(card))
          .returning(_.id)
      })
}
