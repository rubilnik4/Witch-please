package shared.infrastructure.services.storage

import shared.models.files.*
import zio.ZIO

import java.util.UUID

trait FileStorageService:
  def existFile(prefix: String, id: UUID): ZIO[Any, Throwable, Boolean]
  def getFile(prefix: String, id: UUID): ZIO[Any, Throwable, FileBytes]
  def storeFile(prefix: String, fileBytes: FileBytes): ZIO[Any, Throwable, FileStored]
  def deleteFile(prefix: String, id: UUID): ZIO[Any, Throwable, Boolean]
