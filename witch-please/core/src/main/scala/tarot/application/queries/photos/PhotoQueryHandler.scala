package tarot.application.queries.photos

import tarot.domain.models.TarotError
import tarot.domain.models.photo.{Photo, PhotoId}
import tarot.layers.TarotEnv
import zio.ZIO

trait PhotoQueryHandler {
  def getPhoto(photoId: PhotoId): ZIO[TarotEnv, TarotError, Photo]
  def existPhoto(photoId: PhotoId): ZIO[TarotEnv, TarotError, Boolean]
  def existAnyPhoto(photoIds: List[PhotoId]): ZIO[TarotEnv, TarotError, Boolean]
}
