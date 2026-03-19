package tarot.domain.models.cardsOfDay

import shared.models.files.FileStored
import tarot.application.commands.cardsOfDay.commands.UpdateCardOfDayCommand
import tarot.domain.models.cards.CardId
import tarot.domain.models.photo.Photo

import java.util.UUID

final case class CardOfDayUpdate(
  cardId: CardId,
  title: String,
  description: String,
  photo: Photo
)

object CardOfDayUpdate {
  def toDomain(command: UpdateCardOfDayCommand, storedPhoto: FileStored): CardOfDayUpdate =
    val photo = Photo.toPhoto(UUID.randomUUID(), storedPhoto, command.photo.sourceType, command.photo.sourceId)
    CardOfDayUpdate(
      cardId = command.cardId,
      title = command.title,
      description = command.description,
      photo = photo
    )
}
