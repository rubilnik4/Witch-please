package tarot.application.queries.photos

import tarot.domain.models.TarotError
import tarot.domain.models.photo.{Photo, PhotoId}
import tarot.infrastructure.repositories.photo.PhotoRepository
import tarot.layers.TarotEnv
import zio.ZIO

final class PhotoQueryHandlerLive(photoRepository: PhotoRepository) extends PhotoQueryHandler {
  override def existPhoto(photoId: PhotoId): ZIO[TarotEnv, TarotError, Boolean] =
    for {
      _ <- ZIO.logInfo(s"Executing photo exists query by photoId $photoId")
      
      exist <- photoRepository.existPhoto(photoId)
    } yield exist

  override def existAnyPhoto(photoIds: List[PhotoId]): ZIO[TarotEnv, TarotError, Boolean] =
    for {
      _ <- ZIO.logInfo(s"Executing any photo exists query by photoIds $photoIds")

      exist <- photoRepository.existAnyPhoto(photoIds)
    } yield exist
}