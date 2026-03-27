package tarot.infrastructure.repositories.photo

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.application.commands.photos.PhotoDeleteResult
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.photo.{Photo, PhotoId, PhotoObject}
import zio.{ZIO, *}

final class PhotoRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends PhotoRepository {
  private val photoDao = PhotoDao(quill)
  private val photoObjectDao = PhotoObjectDao(quill)

  override def getPhoto(photoId: PhotoId): ZIO[Any, TarotError, Option[Photo]] =
    for {
      _ <- ZIO.logDebug(s"Getting photo $photoId")

      photo <- photoDao.getPhoto(photoId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get photo $photoId", Cause.fail(e)))
        .mapError(e => DatabaseError("Failed to get photo", e))
        .flatMap(photoMaybe => ZIO.foreach(photoMaybe)(PhotoViewEntity.toDomain))
    } yield photo

  override def getPhotoObjectByHash(hash: String): ZIO[Any, TarotError, Option[PhotoObject]] =
    for {
      _ <- ZIO.logDebug(s"Getting photo object by hash $hash")

      photoObject <- photoObjectDao.getPhotoObjectByHash(hash)
        .tapError(e => ZIO.logErrorCause(s"Failed to get photo object by hash $hash", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to get photo object by hash $hash", e))
        .flatMap(photoObjectMaybe => ZIO.foreach(photoObjectMaybe)(PhotoObjectEntity.toDomain))
    } yield photoObject

  override def existPhoto(photoId: PhotoId): ZIO[Any, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Checking photo exist $photoId")
  
      exist <- photoDao.existPhoto(photoId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to check exist photo $photoId", Cause.fail(e)))
        .mapError(e => DatabaseError("Failed to check exist photo", e))
    } yield exist
  
  override def existAnyPhoto(photoIds: List[PhotoId]): ZIO[Any, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Checking photo any exist $photoIds")
  
      exist <- photoDao.existAnyPhoto(photoIds.map(_.id))
        .tapError(e => ZIO.logErrorCause(s"Failed to check exist any photo $photoIds", Cause.fail(e)))
        .mapError(e => DatabaseError("Failed to check exist any photo", e))
    } yield exist
  
  override def deletePhoto(photoId: PhotoId): ZIO[Any, TarotError, PhotoDeleteResult] =
    for {
      _ <- ZIO.logDebug(s"Deleting photo $photoId")

      result <- quill.transaction {
        for {
          photoMaybe <- photoDao.getPhotoForDelete(photoId.id)
          deleteResult <- photoMaybe match {
            case None => ZIO.succeed(PhotoDeleteResult.NotFound)
            case Some(photoDeleteEntity) => deleteExistingPhoto(photoDeleteEntity)
          }
        } yield deleteResult
      }
        .tapError(e => ZIO.logErrorCause(s"Failed to delete photo $photoId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to delete photo $photoId", e))
    } yield result

  private def deleteExistingPhoto(photoDeleteEntity: PhotoDeleteEntity) =
    for {
      _ <- photoDao.deletePhoto(photoDeleteEntity.photoId)
      remainingReferences <- photoDao.countByPhotoObjectId(photoDeleteEntity.photoObjectId)
      deleteResult <- if (remainingReferences == 0L) {
        photoObjectDao.deletePhotoObject(photoDeleteEntity.photoObjectId)
          .as(PhotoDeleteResult.DeletedRecordAndStorage(photoDeleteEntity.fileId))
      } else {
        ZIO.succeed(PhotoDeleteResult.DeletedOnlyRecord)
      }
    } yield deleteResult
}
