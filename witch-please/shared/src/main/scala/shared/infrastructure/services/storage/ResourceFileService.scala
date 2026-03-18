package shared.infrastructure.services.storage

import shared.models.files.FileBytes
import zio.ZIO

trait ResourceFileService:
  def getResourceFile(resourcePath: String): ZIO[Any, Throwable, FileBytes]
