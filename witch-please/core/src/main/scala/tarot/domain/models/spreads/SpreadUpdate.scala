package tarot.domain.models.spreads

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileStored
import shared.models.tarot.photo.PhotoOwnerType
import tarot.application.commands.spreads.commands.UpdateSpreadCommand
import tarot.domain.models.photo.Photo
import zio.UIO

import java.time.*
import java.util.UUID

final case class SpreadUpdate(
  title: String,
  cardCount: Int,
  description: String,
  photo: Photo
)

object SpreadUpdate {
  def toDomain(command: UpdateSpreadCommand, storedPhoto: FileStored): SpreadUpdate =
    val photo = Photo.toPhoto(UUID.randomUUID(), storedPhoto, PhotoOwnerType.Spread,
      command.spreadId.id, command.photo.sourceType, command.photo.sourceId)
    SpreadUpdate(
      title = command.title,
      cardCount = command.cardCount,      
      description = command.description,
      photo = photo
    )
}