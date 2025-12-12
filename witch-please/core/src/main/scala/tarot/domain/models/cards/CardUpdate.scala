package tarot.domain.models.cards

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileStorage
import shared.models.tarot.photo.PhotoOwnerType
import tarot.application.commands.cards.commands.UpdateCardCommand
import tarot.application.commands.spreads.commands.UpdateSpreadCommand
import tarot.domain.models.photo.{Photo, PhotoSource}
import tarot.domain.models.spreads.SpreadUpdate
import zio.UIO

import java.util.UUID

final case class CardUpdate(
  title: String,
  description: String,
  photo: Photo
)

object CardUpdate {
  def toDomain(command: UpdateCardCommand, storedPhoto: FileStorage): UIO[CardUpdate] =
    val photo = Photo.toPhoto(UUID.randomUUID(), storedPhoto, PhotoOwnerType.Spread, command.cardId.id,
      command.photo.sourceType, command.photo.sourceId)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      card = CardUpdate(
        title = command.title,
        description = command.description,
        photo = photo
      )
    } yield card
}
