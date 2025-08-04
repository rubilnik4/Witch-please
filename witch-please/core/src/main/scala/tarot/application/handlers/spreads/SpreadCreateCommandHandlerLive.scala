package tarot.application.handlers.spreads

import tarot.application.commands.spreads.SpreadCreateCommand
import tarot.domain.models.TarotError
import tarot.domain.models.photo.ExternalPhoto
import tarot.domain.models.spreads.*
import tarot.layers.AppEnv
import zio.ZIO


final class SpreadCreateCommandHandlerLive extends SpreadCreateCommandHandler {
  def handle(command: SpreadCreateCommand): ZIO[AppEnv, TarotError, SpreadId] = {
    for {
      _ <- ZIO.logInfo(s"Executing create spread command for ${command.externalSpread}")

      spread <- fetchAndStorePhoto(command.externalSpread)

      spreadRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository.spreadRepository)
      spreadId <- spreadRepository.createSpread(spread)

      _ <- ZIO.logInfo(s"Successfully spread created: $spreadId")
    } yield spreadId
  }

  private def fetchAndStorePhoto(externalSpread: ExternalSpread): ZIO[AppEnv, TarotError, Spread] = {
    for {
      photoService <- ZIO.serviceWith[AppEnv](_.tarotService.photoService)

      storedPhoto <- externalSpread.coverPhotoId match {
        case ExternalPhoto.Telegram(fileId) => photoService.fetchAndStore(fileId)
      }
      spread <- Spread.toDomain(externalSpread, storedPhoto)
    } yield spread
  }
}
