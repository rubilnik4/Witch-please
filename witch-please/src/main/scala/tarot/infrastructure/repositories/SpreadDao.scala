package tarot.infrastructure.repositories

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.{PhotoSourceEntity, SpreadEntity, SpreadPhotoEntity}
import tarot.domain.models.photo.{PhotoOwnerType, PhotoStorageType}
import tarot.domain.models.spreads.SpreadStatus
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class SpreadDao(quill: Quill.Postgres[SnakeCase]) {
  import quill.*
  import QuillMappings.*

  given MappedEncoding[SpreadStatus, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, SpreadStatus] = MappedEncoding(SpreadStatus.valueOf)

  given MappedEncoding[PhotoStorageType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, PhotoStorageType] = MappedEncoding(PhotoStorageType.valueOf)

  given MappedEncoding[PhotoOwnerType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, PhotoOwnerType] = MappedEncoding(PhotoOwnerType.valueOf)

  private inline def spreadTable = quote {
    querySchema[SpreadEntity](TarotTableNames.Spreads)
  }

  private inline def photoTable = quote {
    querySchema[PhotoSourceEntity](TarotTableNames.Photos)
  }

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

  def insertSpread(spread: SpreadEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        spreadTable
          .insertValue(lift(spread))
          .returning(_.id)
      })
}
