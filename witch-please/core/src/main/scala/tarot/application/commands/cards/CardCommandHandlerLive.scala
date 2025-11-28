package tarot.application.commands.cards

import shared.models.files.FileStorage
import tarot.application.commands.cards.commands.CreateCardCommand
import tarot.domain.models.TarotError
import tarot.domain.models.cards.*
import tarot.domain.models.photo.PhotoFile
import tarot.infrastructure.repositories.cards.CardRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.layers.TarotEnv
import zio.ZIO


final class CardCommandHandlerLive(
  spreadRepository: SpreadRepository, 
  cardRepository: CardRepository
) extends CardCommandHandler {

  override def createCard(command: CreateCardCommand): ZIO[TarotEnv, TarotError, CardId] = {
    for {
      _ <- ZIO.logInfo(s"Executing create card ${command.title} command")

      exists <- spreadRepository.existsSpread(command.spreadId)
      _ <- ZIO.unless(exists) {
        ZIO.logError(s"Spread ${command.spreadId} not found for card create") *>
          ZIO.fail(TarotError.NotFound(s"Spread ${command.spreadId} not found"))
      }

      photoSource <- getPhotoSource(command.photo)
      card <- Card.toDomain(command, photoSource)
      cardId <- cardRepository.createCard(card)
    } yield cardId
  }

  private def getPhotoSource(photoFile: PhotoFile): ZIO[TarotEnv, TarotError, FileStorage] = {
    for {
      photoService <- ZIO.serviceWith[TarotEnv](_.tarotService.photoService)
      storedPhoto <- photoService.fetchAndStore(photoFile.fileId)
    } yield storedPhoto
  }
}
