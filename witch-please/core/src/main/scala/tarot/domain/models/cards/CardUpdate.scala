package tarot.domain.models.cards

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileStored
import shared.models.photo.PhotoSource
import shared.models.tarot.photo.PhotoOwnerType
import tarot.application.commands.cards.commands.UpdateCardCommand
import tarot.application.commands.spreads.commands.UpdateSpreadCommand
import tarot.domain.models.photo.Photo
import tarot.domain.models.spreads.SpreadUpdate
import zio.UIO

import java.util.UUID

final case class CardUpdate(
  title: String,
  description: String,
  photo: Photo
)

object CardUpdate {
  def toDomain(command: UpdateCardCommand, storedPhoto: FileStored): CardUpdate =
    val photo = Photo.toPhoto(UUID.randomUUID(), storedPhoto, PhotoOwnerType.Spread, command.cardId.id,
      command.photo.sourceType, command.photo.sourceId)
    CardUpdate(
      title = command.title,
      description = command.description,
      photo = photo
    )
}
