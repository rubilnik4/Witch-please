package tarot.infrastructure.repositories.photo

import tarot.domain.models.TarotError
import tarot.domain.models.photo.{Photo, PhotoId}
import tarot.layers.TarotEnv
import zio.ZIO

trait PhotoRepository {
  def getPhoto(photoId: PhotoId): ZIO[Any, TarotError, Option[Photo]]
  def existPhoto(photoId: PhotoId): ZIO[Any, TarotError, Boolean]
  def existAnyPhoto(photoIds: List[PhotoId]): ZIO[Any, TarotError, Boolean]
  def deletePhoto(photoId: PhotoId): ZIO[Any, TarotError, Boolean]
}
