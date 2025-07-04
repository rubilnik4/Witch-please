package tarot.infrastructure.repositories

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.{CardEntity, PhotoEntity, SpreadEntity, SpreadPhotoEntity}
import tarot.domain.models.spreads.SpreadStatus
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class SpreadDao(quill: Quill.Postgres[SnakeCase]) {
  import QuillMappings.given
  import quill.*

  def getSpread(spreadId: UUID): ZIO[Any, SQLException, Option[SpreadPhotoEntity]] =
    run(
      quote {
        spreadTable
          .join(photoTable)
          .on((spread, photo) => spread.coverPhotoId == photo.id)
          .filter { case (spread, _) => spread.id == lift(spreadId) }
          .take(1)
          .map { case (spread, photo) => SpreadPhotoEntity(spread, photo) }
      })
      .map(_.headOption)

  def existsSpread(spreadId: UUID): ZIO[Any, SQLException, Boolean] =
    run(
      quote {
        spreadTable
          .filter { spread => spread.id == lift(spreadId) }
          .take(1)
          .nonEmpty
      })

  def validateSpread(spreadId: UUID): ZIO[Any, SQLException, Boolean] =
    run(
      quote {
        spreadTable
          .filter(_.id == lift(spreadId))
          .join(cardTable)
          .on((spread, card) => spread.id == card.spreadId)
          .groupBy(_._1)
          .map { case (spread, grouped) => (spread.id, spread.cardCount, grouped.size)}
          .filter { case (_, expected, actual) => expected == actual }
          .take(1)
          .nonEmpty
    })

  def insertSpread(spread: SpreadEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        spreadTable
          .insertValue(lift(spread))
          .returning(_.id)
      })

  def updateSpreadStatus(spreadId: UUID, spreadStatus: SpreadStatus): ZIO[Any, SQLException, Long] =
    run(
      quote {
        spreadTable
          .filter(_.id == lift(spreadId))
          .update(_.spreadStatus -> lift(spreadStatus))
      }
    )
    
  private inline def spreadTable = quote {
    querySchema[SpreadEntity](TarotTableNames.Spreads)
  }

  private inline def cardTable = quote {
    querySchema[CardEntity](TarotTableNames.Cards)
  }

  private inline def photoTable = quote {
    querySchema[PhotoEntity](TarotTableNames.Photos)
  }
}
