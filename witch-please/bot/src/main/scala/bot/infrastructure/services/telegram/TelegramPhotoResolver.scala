package bot.infrastructure.services.telegram

import shared.api.dto.tarot.photo.PhotoResponse
import shared.models.files.FileSourceType
import zio.{IO, ZIO}

object TelegramPhotoResolver {
  def getFileId(photo: PhotoResponse): IO[Throwable, String] =
    photo.sourceType match {
      case FileSourceType.Telegram =>
        ZIO.succeed(photo.sourceId)
      case other =>
        ZIO.fail(RuntimeException(s"Photo ${photo.id} is not available as telegram fileId (sourceType=$other)"))
    }
}
