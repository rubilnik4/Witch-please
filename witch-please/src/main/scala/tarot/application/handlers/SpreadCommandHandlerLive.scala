package tarot.application.handlers

import tarot.application.commands.SpreadCommand
import tarot.domain.models.TarotError
import tarot.domain.models.contracts.SpreadId
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.{ExternalSpread, Spread, SpreadMapper}
import tarot.layers.AppEnv
import zio.ZIO

import java.time.Instant

final class SpreadCommandHandlerLive extends SpreadCommandHandler {
  override def handle(command: SpreadCommand): ZIO[AppEnv, TarotError, SpreadId] = {
    for {
      _ <- ZIO.logInfo(s"Executing spread command for ${command.externalSpread}")

      spread <- fetchAndStorePhoto(command.externalSpread)

      tarotRepository <- ZIO.serviceWith[AppEnv](_.tarotRepository)
      spreadId <- tarotRepository.createSpread(spread)

      _ <- ZIO.logInfo(s"Successfully spread created: $spread")
    } yield spreadId
  }

  private def fetchAndStorePhoto(externalSpread: ExternalSpread): ZIO[AppEnv, TarotError, Spread] = {
    for {
      photoService <- ZIO.serviceWith[AppEnv](_.tarotService.photoService)

      coverPhoto <- externalSpread.coverPhotoId match {
        case PhotoSource.Telegram(fileId) => photoService.fetchAndStore(fileId)
        case other =>
          ZIO.fail(
            TarotError.SerializationError(
              s"Unresolved photo store location type: ${other.getClass.getSimpleName}"
            )
          )
            .tapError(err => ZIO.logError(s"Failed to fetch photo: ${externalSpread.coverPhotoId}. Error: $err"))
      }
      spread = SpreadMapper.fromExternal(externalSpread, coverPhoto)
    } yield spread
  }
}
