package tarot.domain.models.cards

import shared.infrastructure.services.common.DateTimeService
import shared.models.photo.PhotoFile
import shared.models.tarot.cards.CardPosition
import tarot.application.commands.cards.commands.CreateCardCommand
import tarot.domain.models.photo.Photo
import tarot.domain.models.spreads.SpreadId
import zio.UIO

import java.time.Instant
import java.util.UUID

final case class Card(
  id: CardId,
  position: Int,
  spreadId: SpreadId,
  title: String,
  description: String,
  photo: Photo,
  createdAt: Instant
)

object Card {
  def toDomain(command: CreateCardCommand, photoFile: PhotoFile): UIO[Card] =
    val photo = Photo.create(photoFile, command.photo.sourceType, command.photo.sourceId)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      card = Card(
        id = CardId(UUID.randomUUID()),
        position = command.position,
        spreadId = command.spreadId,
        title = command.title,
        description = command.description,
        photo = photo,
        createdAt = createdAt)
    } yield card

  def clone(card: Card, spreadId: SpreadId, photoFile: PhotoFile): UIO[Card] =
    val photo = Photo.create(photoFile, card.photo.sourceType, card.photo.sourceId)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      cloneCard = Card(
        id = CardId(UUID.randomUUID()),
        position = card.position,
        spreadId = spreadId,
        title = card.title,
        description = card.description,
        photo = photo,
        createdAt = createdAt)
    } yield cloneCard
    
  def toCardPosition(card: Card): CardPosition =
    CardPosition(card.position, card.id.id)  
}
