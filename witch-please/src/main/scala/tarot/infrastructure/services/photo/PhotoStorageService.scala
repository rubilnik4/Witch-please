package tarot.infrastructure.services.photo

import tarot.domain.models.TarotError
import zio.ZIO

trait PhotoStorageService:
  def storePhoto(fileName: String, bytes: Array[Byte]): ZIO[Any, TarotError, String]
