package tarot.infrastructure.repositories.photo

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.photo.{Photo, PhotoId}
import tarot.layers.TarotEnv
import zio.{ZIO, *}

import java.sql.SQLException

final class PhotoRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends PhotoRepository {
  private val photoDao = PhotoDao(quill)

  override def getPhoto(photoId: PhotoId): ZIO[Any, TarotError, Option[Photo]] =
    for {
      _ <- ZIO.logDebug(s"Getting photo $photoId")

      photo <- photoDao.getSpread(photoId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to get photo $photoId", Cause.fail(e)))
        .mapError(e => DatabaseError("Failed to get photo", e))
        .flatMap(photoMaybe => ZIO.foreach(photoMaybe)(PhotoEntity.toDomain))
    } yield photo

  override def deletePhoto(photoId: PhotoId): ZIO[Any, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Deleting photo $photoId")

      count <- photoDao.deletePhoto(photoId.id)
        .tapError(e => ZIO.logErrorCause(s"Failed to delete photo $photoId", Cause.fail(e)))
        .mapError(e => DatabaseError(s"Failed to delete photo $photoId", e))
    } yield count > 0 
}
