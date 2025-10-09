package tarot.domain.models.cards

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileSource
import shared.models.tarot.photo.PhotoOwnerType
import tarot.domain.models.photo.*
import tarot.domain.models.spreads.SpreadId
import zio.UIO

import java.time.Instant
import java.util.UUID

final case class Card(
  id: CardId,
  spreadId: SpreadId,
  description: String,
  photo: Photo,
  createdAt: Instant
)
{
  override def toString: String = id.toString
}

object Card {
  def toDomain(externalCard: ExternalCard, storedPhoto: FileSource): UIO[Card] =
    val id = UUID.randomUUID()
    val externalPhotoId = ExternalPhoto.getFileId(externalCard.coverPhoto)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      card = Card(
        id = CardId(id),
        spreadId = externalCard.spreadId,
        description = externalCard.description,
        photo = Photo.toPhoto(storedPhoto, PhotoOwnerType.Card, id, externalPhotoId),
        createdAt = createdAt)
    } yield card
}