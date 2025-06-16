package tarot.infrastructure.services.photo

import tarot.domain.models.TarotError
import tarot.domain.models.photo.PhotoLocation
import tarot.layers.AppEnv
import zio.ZIO

trait PhotoService {
  def fetchAndStore(fileId: String): ZIO[Any, TarotError, PhotoLocation]
}
