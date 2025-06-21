package tarot.infrastructure.repositories

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.{PhotoSourceEntity, PhotoStorageType, SpreadEntity}
import tarot.domain.models.spreads.SpreadStatus
import zio.ZIO
import io.getquill.MappedEncoding

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class PhotoDao(quill: Quill.Postgres[SnakeCase]) {
  import quill.*

  given MappedEncoding[PhotoStorageType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, PhotoStorageType] = MappedEncoding(PhotoStorageType.valueOf)

  private inline def photoTable = quote {
    querySchema[PhotoSourceEntity](TarotTableNames.Photos)
  }

  def insertPhoto(photo: PhotoSourceEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        photoTable
          .insertValue(lift(photo))
          .returning(_.id)
      })
}