package tarot.infrastructure.services.photo

import shared.infrastructure.services.storage.FileStorageService
import shared.infrastructure.services.telegram.*
import shared.models.files.*
import shared.models.photo.{PhotoFile, PhotoSource}
import tarot.domain.models.{TarotError, TarotErrorMapper}
import zio.ZIO

final class PhotoServiceLive(
  telegram: TelegramApiService, 
  storage: FileStorageService
) extends PhotoService:
  
  override def fetchAndStore(photoSource: PhotoSource): ZIO[Any, TarotError, PhotoFile] =
    for {
      _ <- ZIO.logDebug(s"Fetch and store photo: ${photoSource.sourceId}")
      
      fileId <- TelegramPhotoResolver.getFileId(photoSource)
        .mapError(error => TarotError.UnsupportedType(error.getMessage))
      
      telegramFile <- telegram.downloadPhoto(fileId).mapError(TarotErrorMapper.toTarotError)
      fileBytes = FileBytes(telegramFile.fileName, telegramFile.bytes)
      fileStored <- storage.storeFile(fileBytes)
        .mapError(error => TarotError.StorageError(error.getMessage, error.getCause))
      
    } yield PhotoFile(fileStored, photoSource)

  override def fetchAndStore(photoSources: List[PhotoSource], parallelism: Int): ZIO[Any, TarotError, List[PhotoFile]] =
    ZIO.withParallelism(parallelism.max(parallelism)) {
      ZIO.foreachPar(photoSources)(fetchAndStore)
    }