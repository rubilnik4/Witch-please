package tarot.application.commands.spreads

import tarot.application.commands.spreads.SpreadCreateCommand
import tarot.domain.models.TarotError
import tarot.domain.models.photo.ExternalPhoto
import tarot.domain.models.spreads.*
import tarot.layers.TarotEnv
import zio.ZIO


final class SpreadCreateCommandHandlerLive extends SpreadCreateCommandHandler {
  def handle(command: SpreadCreateCommand): ZIO[TarotEnv, TarotError, SpreadId] = {
    for {
      _ <- ZIO.logInfo(s"Executing create spread command for ${command.externalSpread}")

      spread <- fetchAndStorePhoto(command.externalSpread)

      spreadRepository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.spreadRepository)
      spreadId <- spreadRepository.createSpread(spread)

      _ <- ZIO.logInfo(s"Successfully spread created: $spreadId")
    } yield spreadId
  }

  private def fetchAndStorePhoto(externalSpread: ExternalSpread): ZIO[TarotEnv, TarotError, Spread] = {
    for {
      photoService <- ZIO.serviceWith[TarotEnv](_.tarotService.photoService)

      storedPhoto <- externalSpread.coverPhoto match {
        case ExternalPhoto.Telegram(fileId) => photoService.fetchAndStore(fileId)
      }
      spread <- Spread.toDomain(externalSpread, storedPhoto)
    } yield spread
  }
}
