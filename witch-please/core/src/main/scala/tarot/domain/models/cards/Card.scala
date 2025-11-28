package tarot.domain.models.cards

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileStorage
import shared.models.tarot.photo.PhotoOwnerType
import tarot.domain.models.photo.*
import tarot.domain.models.spreads.SpreadId
import zio.UIO

import java.time.Instant
import java.util.UUID

final case class Card(
  id: CardId,
  index: Int,
  spreadId: SpreadId,
  description: String,
  photo: Photo,
  createdAt: Instant
)
{
  override def toString: String = id.toString
}

object Card {
  def toDomain(externalCard: ExternalCard, storedPhoto: FileStorage): UIO[Card] =
    val id = UUID.randomUUID()
    val coverPhoto = externalCard.coverPhoto
    val photo = Photo.toPhoto(storedPhoto, PhotoOwnerType.Card, id, coverPhoto.sourceType, coverPhoto.fileId)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      card = Card(
        id = CardId(id),
        index = externalCard.index,
        spreadId = externalCard.spreadId,
        description = externalCard.description,
        photo = photo,
        createdAt = createdAt)
    } yield card
}