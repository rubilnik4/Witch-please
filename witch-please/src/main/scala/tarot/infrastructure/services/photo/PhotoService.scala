package tarot.infrastructure.services.photo

import tarot.domain.models.TarotError
import tarot.domain.models.photo.{PhotoFile, Photo, PhotoSource}
import tarot.layers.AppEnv
import zio.ZIO

trait PhotoService {
  def fetchAndStore(fileId: String): ZIO[Any, TarotError, PhotoSource]
}
