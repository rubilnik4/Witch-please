package tarot.application.commands.photos

import shared.infrastructure.services.storage.StoragePrefix
import tarot.domain.models.TarotError
import tarot.domain.models.photo.PhotoId
import tarot.infrastructure.repositories.photo.PhotoRepository
import tarot.layers.TarotEnv
import zio.ZIO

final class PhotoCommandHandlerLive(photoRepository: PhotoRepository) extends PhotoCommandHandler {
  override def deletePhoto(photoId: PhotoId): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing delete photo command for $photoId")

      fileStorageService <- ZIO.serviceWith[TarotEnv](_.services.fileStorageService)

      deleteResult <- photoRepository.deletePhoto(photoId)
      _ <- deleteResult match {
        case PhotoDeleteResult.NotFound =>
          ZIO.logWarning(s"Photo $photoId not found in database during delete")
        case PhotoDeleteResult.DeletedOnlyRecord =>
          ZIO.unit
        case PhotoDeleteResult.DeletedRecordAndStorage(fileId) =>
          for {
            deletedFromStorage <- fileStorageService.deleteFile(StoragePrefix.photo, fileId)
              .mapError(err => TarotError.StorageError(err.getMessage, err.getCause))
            _ <- ZIO.logWarning(s"File $fileId not found in storage during delete")
              .when(!deletedFromStorage)
          } yield ()
      }
    } yield ()
}
