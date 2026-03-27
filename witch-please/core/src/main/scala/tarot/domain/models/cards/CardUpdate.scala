package tarot.domain.models.cards

import shared.models.photo.PhotoFile
import tarot.application.commands.cards.commands.UpdateCardCommand
import tarot.domain.models.photo.Photo

final case class CardUpdate(
  title: String,
  description: String,
  photo: Photo
)

object CardUpdate {
  def toDomain(command: UpdateCardCommand, storedPhoto: PhotoFile): CardUpdate =
    val photo = Photo.create(storedPhoto, command.photo.sourceType, command.photo.sourceId)
    CardUpdate(
      title = command.title,
      description = command.description,
      photo = photo
    )
}
