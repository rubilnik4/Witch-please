package tarot.domain.models.cards

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileStorage
import shared.models.tarot.photo.PhotoOwnerType
import tarot.application.commands.cards.commands.CreateCardCommand
import tarot.domain.models.photo.*
import tarot.domain.models.spreads.SpreadId
import zio.UIO

import java.time.Instant
import java.util.UUID

final case class Card(
  id: CardId,
  index: Int,
  spreadId: SpreadId,
  title: String,
  photo: Photo,
  createdAt: Instant
)

object Card {
  def toDomain(command: CreateCardCommand, photoFile: FileStorage): UIO[Card] =
    val id = UUID.randomUUID()
    val photoSource = command.photo
    val photo = Photo.toPhoto(UUID.randomUUID(), photoFile, PhotoOwnerType.Card, id, photoSource.sourceType, photoSource.sourceId)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      card = Card(
        id = CardId(id),
        index = command.index,
        spreadId = command.spreadId,
        title = command.title,
        photo = photo,
        createdAt = createdAt)
    } yield card
}