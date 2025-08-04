package tarot.infrastructure.services.photo

import tarot.domain.models.TarotError
import tarot.domain.models.photo.{PhotoFile, PhotoSource}
import zio.ZIO

trait FileStorageService:
  def storePhoto(photoFile: PhotoFile): ZIO[Any, TarotError, PhotoSource]
  def getResourcePhoto(resourcePath: String): ZIO[Any, TarotError, PhotoFile]
