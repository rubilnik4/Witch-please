package shared.infrastructure.services.telegram

import shared.models.files.FileSourceType
import shared.models.photo.PhotoSource
import zio.{IO, ZIO}

object TelegramPhotoResolver {
  def getFileId(photoSource: PhotoSource): IO[Throwable, String] =
    photoSource.sourceType match {
      case FileSourceType.Telegram =>
        ZIO.succeed(photoSource.sourceId)
      case other =>
        ZIO.logError(s"Photo ${photoSource.sourceId} is not available as telegram fileId (sourceType=$other)") *>
          ZIO.fail(RuntimeException(s"Photo ${photoSource.sourceId} is not available as telegram fileId (sourceType=$other)"))
    }
}
