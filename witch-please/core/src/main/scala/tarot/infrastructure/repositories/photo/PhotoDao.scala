package tarot.infrastructure.repositories.photo

import io.getquill.*
import io.getquill.jdbczio.*
import shared.models.tarot.photo.PhotoOwnerType
import tarot.domain.entities.{PhotoEntity, SpreadPhotoEntity}
import tarot.infrastructure.repositories.TarotTableNames
import tarot.infrastructure.repositories.spreads.SpreadQuillMappings
import zio.ZIO

import java.sql.SQLException
import java.util.UUID

final class PhotoDao(quill: Quill.Postgres[SnakeCase]) {
  import PhotoQuillMappings.given
  import quill.*

  def getSpread(photoId: UUID): ZIO[Any, SQLException, Option[PhotoEntity]] =
    run(
      quote {
        photoTable
          .filter { photo => photo.id == lift(photoId) }
          .take(1)
      })
      .map(_.headOption)
      
  def insertPhoto(photo: PhotoEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        photoTable
          .insertValue(lift(photo))
          .returning(_.id)
      })

  def deletePhoto(photoId: UUID): ZIO[Any, SQLException, Long] =
    run(
      quote {
        photoTable
          .filter(_.id == lift(photoId))
          .delete
      })

  private inline def photoTable =
    quote(querySchema[PhotoEntity](TarotTableNames.photos))
}