package shared.infrastructure.services.storage

import shared.models.files.*
import zio.ZIO

import java.util.UUID

trait FileStorageService:
  def storeFile(storedFile: FileBytes): ZIO[Any, Throwable, FileStored]
  def deleteFile(id: UUID): ZIO[Any, Throwable, Boolean]
  def getResourceFile(resourcePath: String): ZIO[Any, Throwable, FileBytes]
