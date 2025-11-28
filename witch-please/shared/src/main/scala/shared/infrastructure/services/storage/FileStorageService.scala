package shared.infrastructure.services.storage

import shared.models.files.*
import zio.ZIO

trait FileStorageService:
  def storePhoto(storedFile: StoredFile): ZIO[Any, Throwable, FileStorage]
  def getResourcePhoto(resourcePath: String): ZIO[Any, Throwable, StoredFile]
