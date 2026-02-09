package tarot.application.queries.photos

import tarot.domain.models.TarotError
import tarot.domain.models.photo.{Photo, PhotoId}
import tarot.infrastructure.repositories.photo.PhotoRepository
import tarot.layers.TarotEnv
import zio.ZIO

final class PhotoQueryHandlerLive(photoRepository: PhotoRepository) extends PhotoQueryHandler {
  override def getPhoto(photoId: PhotoId): ZIO[TarotEnv, TarotError, Photo] =
    for {
      _ <- ZIO.logDebug(s"Executing photo query by photoId $photoId")

      photo <- photoRepository.getPhoto(photoId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Photo $photoId not found")))
        .tapError(_ => ZIO.logError(s"Photo $photoId not found"))
    } yield photo

  override def existPhoto(photoId: PhotoId): ZIO[TarotEnv, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Executing photo exists query by photoId $photoId")
      
      exist <- photoRepository.existPhoto(photoId)
    } yield exist

  override def existAnyPhoto(photoIds: List[PhotoId]): ZIO[TarotEnv, TarotError, Boolean] =
    for {
      _ <- ZIO.logDebug(s"Executing any photo exists query by photoIds $photoIds")

      exist <- photoRepository.existAnyPhoto(photoIds)
    } yield exist
}