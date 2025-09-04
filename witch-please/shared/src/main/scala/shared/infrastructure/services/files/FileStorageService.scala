package shared.infrastructure.services.files

import shared.models.files.*
import zio.ZIO

trait FileStorageService:
  def storePhoto(storedFile: StoredFile): ZIO[Any, Throwable, FileSource]
  def getResourcePhoto(resourcePath: String): ZIO[Any, Throwable, StoredFile]
