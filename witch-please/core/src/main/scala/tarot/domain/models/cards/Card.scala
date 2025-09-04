package tarot.domain.models.cards

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileSource
import tarot.domain.models.photo.*
import tarot.domain.models.spreads.SpreadId
import zio.UIO

import java.time.Instant
import java.util.UUID

final case class Card(
  id: CardId,
  spreadId: SpreadId,
  description: String,
  coverPhoto: Photo,
  createdAt: Instant
)
{
  override def toString: String = id.toString
}

object Card {
  def toDomain(externalCard: ExternalCard, storedPhoto: FileSource): UIO[Card] =
    val id = UUID.randomUUID()
    for {
      createdAt <- DateTimeService.getDateTimeNow
      card = Card(
        id = CardId(id),
        spreadId = externalCard.spreadId,
        description = externalCard.description,
        coverPhoto = Photo.toPhotoSource(storedPhoto, PhotoOwnerType.Card, id),
        createdAt = createdAt)
    } yield card
}