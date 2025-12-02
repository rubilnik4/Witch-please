package tarot.application.queries.photos

import tarot.domain.models.TarotError
import tarot.domain.models.photo.{Photo, PhotoId}
import tarot.infrastructure.repositories.photo.PhotoRepository
import tarot.layers.TarotEnv
import zio.ZIO

final class PhotoQueryHandlerLive(photoRepository: PhotoRepository) extends PhotoQueryHandler {
  def getPhoto(photoId: PhotoId): ZIO[TarotEnv, TarotError, Photo] =
    for {
      _ <- ZIO.logInfo(s"Executing photo query by photoId $photoId")
      
      photo <- photoRepository.getPhoto(photoId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Photo $photoId not found")))
    } yield photo
}