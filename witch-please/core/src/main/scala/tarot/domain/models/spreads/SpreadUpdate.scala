package tarot.domain.models.spreads

import shared.models.photo.PhotoFile
import tarot.application.commands.spreads.commands.UpdateSpreadCommand
import tarot.domain.models.photo.Photo

final case class SpreadUpdate(
  title: String,
  cardCount: Int,
  description: String,
  photo: Photo
)

object SpreadUpdate {
  def toDomain(command: UpdateSpreadCommand, storedPhoto: PhotoFile): SpreadUpdate =
    val photo = Photo.create(storedPhoto, command.photo.sourceType, command.photo.sourceId)
    SpreadUpdate(
      title = command.title,
      cardCount = command.cardCount,      
      description = command.description,
      photo = photo
    )
}
