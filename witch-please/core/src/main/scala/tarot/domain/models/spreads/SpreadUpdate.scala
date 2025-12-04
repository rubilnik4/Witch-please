package tarot.domain.models.spreads

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileStorage
import shared.models.tarot.photo.PhotoOwnerType
import tarot.application.commands.spreads.commands.UpdateSpreadCommand
import tarot.domain.models.photo.Photo
import zio.UIO

import java.time.*
import java.util.UUID

final case class SpreadUpdate(
  title: String,
  cardCount: Int,
  photo: Photo
)

object SpreadUpdate {
  def toDomain(command: UpdateSpreadCommand, storedPhoto: FileStorage): UIO[SpreadUpdate] =
    val photo = Photo.toPhoto(UUID.randomUUID(), storedPhoto, PhotoOwnerType.Spread,
      command.spreadId.id, command.photo.sourceType, command.photo.sourceId)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      spread = SpreadUpdate(
        title = command.title,
        cardCount = command.cardCount,      
        photo = photo
      )
    } yield spread
}