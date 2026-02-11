package tarot.domain.models.cardsOfDay

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileStored
import shared.models.photo.PhotoSource
import shared.models.tarot.photo.PhotoOwnerType
import tarot.application.commands.cards.commands.UpdateCardCommand
import tarot.application.commands.cardsOfDay.commands.UpdateCardOfDayCommand
import tarot.application.commands.spreads.commands.UpdateSpreadCommand
import tarot.domain.models.cards.CardId
import tarot.domain.models.photo.Photo
import tarot.domain.models.spreads.SpreadUpdate
import zio.UIO

import java.util.UUID

final case class CardOfDayUpdate(
  cardId: CardId,
  title: String,
  description: String,
  photo: Photo
)

object CardOfDayUpdate {
  def toDomain(command: UpdateCardOfDayCommand, storedPhoto: FileStored): CardOfDayUpdate =
    val photo = Photo.toPhoto(UUID.randomUUID(), storedPhoto, PhotoOwnerType.Spread, command.cardId.id,
      command.photo.sourceType, command.photo.sourceId)   
    CardOfDayUpdate(
      cardId = command.cardId,
      title = command.title,
      description = command.description,
      photo = photo
    )
}
