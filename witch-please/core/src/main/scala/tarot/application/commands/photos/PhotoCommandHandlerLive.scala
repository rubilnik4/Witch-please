package tarot.application.commands.photos

import tarot.domain.models.TarotError
import tarot.domain.models.photo.PhotoId
import tarot.infrastructure.repositories.photo.PhotoRepository
import tarot.layers.TarotEnv
import zio.ZIO

import java.util.UUID

final class PhotoCommandHandlerLive(photoRepository: PhotoRepository) extends PhotoCommandHandler {
  override def deletePhoto(photoId: PhotoId, fileId: UUID): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Executing delete photo command for $photoId")

      deletedFromDb <- photoRepository.deletePhoto(photoId)
      _ <- ZIO.logWarning(s"Photo $photoId not found in DB during delete")
        .when(!deletedFromDb)

      fileStorageService <- ZIO.serviceWith[TarotEnv](_.services.fileStorageService)
      deletedFromStorage <- fileStorageService.deleteFile(fileId)
        .mapError(err => TarotError.StorageError(err.getMessage, err.getCause))
      _ <- ZIO.logWarning(s"File $fileId not found in storage during delete")
        .when(!deletedFromStorage)
    } yield ()
  }

