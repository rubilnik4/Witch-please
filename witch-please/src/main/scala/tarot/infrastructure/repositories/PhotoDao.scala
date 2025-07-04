package tarot.infrastructure.repositories

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.PhotoEntity
import tarot.domain.models.photo.{PhotoOwnerType, PhotoStorageType}
import zio.ZIO

import java.sql.SQLException
import java.util.UUID

final class PhotoDao(quill: Quill.Postgres[SnakeCase]) {
  import quill.*
  import QuillMappings.given
  
  private inline def photoTable = quote {
    querySchema[PhotoEntity](TarotTableNames.Photos)
  }

  def insertPhoto(photo: PhotoEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        photoTable
          .insertValue(lift(photo))
          .returning(_.id)
      })
}