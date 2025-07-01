package tarot.infrastructure.services.photo

import tarot.domain.models.TarotError
import tarot.domain.models.photo.{PhotoFile, PhotoSource, StoredPhotoSource}
import zio.ZIO

trait FileStorageService:
  def storePhoto(photoFile: PhotoFile): ZIO[Any, TarotError, StoredPhotoSource]
  def getResourcePhoto(resourcePath: String): ZIO[Any, TarotError, PhotoFile]
