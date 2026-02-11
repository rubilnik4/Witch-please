package tarot.infrastructure.services.photo

import shared.models.files.*
import shared.models.photo.{PhotoFile, PhotoSource}
import tarot.domain.models.TarotError
import tarot.domain.models.photo.*
import zio.ZIO

trait PhotoService {
  def fetchAndStore(photoSource: PhotoSource): ZIO[Any, TarotError, PhotoFile]
  def fetchAndStore(photoSources: List[PhotoSource], parallelism: Int = 4): ZIO[Any, TarotError, List[PhotoFile]]
}
