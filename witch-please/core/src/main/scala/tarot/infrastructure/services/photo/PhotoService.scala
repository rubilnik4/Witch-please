package tarot.infrastructure.services.photo

import shared.models.files.FileSource
import tarot.domain.models.TarotError

import zio.ZIO

trait PhotoService {
  def fetchAndStore(fileId: String): ZIO[Any, TarotError, FileSource]
}
