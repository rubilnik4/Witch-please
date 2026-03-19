package tarot.domain.models.cards

import shared.models.files.FileStored
import tarot.application.commands.cards.commands.UpdateCardCommand
import tarot.domain.models.photo.Photo

import java.util.UUID

final case class CardUpdate(
  title: String,
  description: String,
  photo: Photo
)

object CardUpdate {
  def toDomain(command: UpdateCardCommand, storedPhoto: FileStored): CardUpdate =
    val photo = Photo.toPhoto(UUID.randomUUID(), storedPhoto, command.photo.sourceType, command.photo.sourceId)
    CardUpdate(
      title = command.title,
      description = command.description,
      photo = photo
    )
}
