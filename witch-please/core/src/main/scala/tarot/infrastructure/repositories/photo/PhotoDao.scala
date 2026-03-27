package tarot.infrastructure.repositories.photo

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.{PhotoDeleteEntity, PhotoEntity, PhotoObjectEntity, PhotoViewEntity}
import tarot.infrastructure.repositories.TarotTableNames
import zio.ZIO

import java.sql.SQLException
import java.util.UUID

final class PhotoDao(quill: Quill.Postgres[SnakeCase]) {
  import PhotoQuillMappings.given
  import quill.*

  def getPhoto(photoId: UUID): ZIO[Any, SQLException, Option[PhotoViewEntity]] =
    run(
      quote {
        photoTable
          .join(photoObjectTable)
          .on(_.photoObjectId == _.id)
          .filter { case (photo, _) => photo.id == lift(photoId) }
          .take(1)
          .map { case (photo, photoObject) =>
            PhotoDao.photoViewEntity(photo, photoObject)
          }
      })
      .map(_.headOption)

  def getPhotoForDelete(photoId: UUID): ZIO[Any, SQLException, Option[PhotoDeleteEntity]] =
    run(
      quote {
        photoTable
          .join(photoObjectTable)
          .on(_.photoObjectId == _.id)
          .filter { case (photo, _) => photo.id == lift(photoId) }
          .take(1)
          .map { case (photo, photoObject) =>
            PhotoDeleteEntity(
              photoId = photo.id,
              photoObjectId = photo.photoObjectId,
              fileId = photoObject.fileId
            )
          }
      })
      .map(_.headOption)

  def getPhotoEntity(photoId: UUID): ZIO[Any, SQLException, Option[PhotoEntity]] =
    run(
      quote {
        photoTable
          .filter { photo => photo.id == lift(photoId) }
          .take(1)
      })
      .map(_.headOption)

  def existPhoto(photoId: UUID): ZIO[Any, SQLException, Boolean] =
    run(
      quote {
        photoTable
          .filter { photo => photo.id == lift(photoId) }
          .take(1).
          nonEmpty
      })

  def existAnyPhoto(photoIds: List[UUID]): ZIO[Any, SQLException, Boolean] =
    run(
      quote {
        photoTable
          .filter { photo => lift(photoIds).contains(photo.id) }
          .take(1).
          nonEmpty
      })

  def countByPhotoObjectId(photoObjectId: UUID): ZIO[Any, SQLException, Long] =
    run(
      quote {
        photoTable
          .filter(_.photoObjectId == lift(photoObjectId))
          .size
      }
    )

  def insertPhoto(photo: PhotoEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        photoTable
          .insertValue(lift(photo))
          .returning(_.id)
      })

  def insertPhotos(photos: List[PhotoEntity]): ZIO[Any, SQLException, List[UUID]] =
    run(
      quote {
        liftQuery(photos).foreach { photo =>
          photoTable.insertValue(photo).returning(_.id)
        }
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

  private inline def photoObjectTable =
    quote(querySchema[PhotoObjectEntity](TarotTableNames.photoObjects))
}

object PhotoDao {
  inline def photoViewEntity(photoEntity: PhotoEntity, photoObjectEntity: PhotoObjectEntity) =
    PhotoViewEntity(
      id = photoEntity.id,
      sourceType = photoEntity.sourceType,
      sourceId = photoEntity.sourceId,
      fileId = photoObjectEntity.fileId,
      hash = photoObjectEntity.hash,
      storageType = photoObjectEntity.storageType,
      path = photoObjectEntity.path,
      bucket = photoObjectEntity.bucket,
      key = photoObjectEntity.key
    )
}
