package tarot.domain.models.cardsOfDay

import shared.models.photo.PhotoFile
import tarot.application.commands.cardsOfDay.commands.UpdateCardOfDayCommand
import tarot.domain.models.cards.CardId
import tarot.domain.models.photo.Photo

final case class CardOfDayUpdate(
  cardId: CardId,
  title: String,
  description: String,
  photo: Photo
)

object CardOfDayUpdate {
  def toDomain(command: UpdateCardOfDayCommand, storedPhoto: PhotoFile): CardOfDayUpdate =
    val photo = Photo.create(storedPhoto, command.photo.sourceType, command.photo.sourceId)
    CardOfDayUpdate(
      cardId = command.cardId,
      title = command.title,
      description = command.description,
      photo = photo
    )
}
