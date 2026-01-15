package tarot.domain.models.cardsOfDay

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileStorage
import shared.models.tarot.photo.PhotoOwnerType
import tarot.application.commands.cards.commands.UpdateCardCommand
import tarot.application.commands.cardsOfDay.commands.UpdateCardOfDayCommand
import tarot.application.commands.spreads.commands.UpdateSpreadCommand
import tarot.domain.models.cards.CardId
import tarot.domain.models.photo.{Photo, PhotoSource}
import tarot.domain.models.spreads.SpreadUpdate
import zio.UIO

import java.util.UUID

final case class CardOfDayUpdate(
  cardId: CardId,
  description: String,
  photo: Photo
)

object CardOfDayUpdate {
  def toDomain(command: UpdateCardOfDayCommand, storedPhoto: FileStorage): CardOfDayUpdate =
    val photo = Photo.toPhoto(UUID.randomUUID(), storedPhoto, PhotoOwnerType.Spread, command.cardId.id,
      command.photo.sourceType, command.photo.sourceId)   
    CardOfDayUpdate(
      cardId = command.cardId,
      description = command.description,
      photo = photo
    )
}
