package tarot.domain.entities

import shared.models.tarot.cardOfDay.CardOfDayStatus
import tarot.domain.models.TarotError
import tarot.domain.models.cards.CardId
import tarot.domain.models.cardsOfDay.{CardOfDay, CardOfDayId}
import tarot.domain.models.spreads.SpreadId
import zio.ZIO

import java.time.Instant
import java.util.UUID

final case class CardOfDayPhotoEntity(
  cardOfDayId: UUID,
  cardId: UUID,
  spreadId: UUID,
  title: String,
  description: String,
  status: CardOfDayStatus,
  createdAt: Instant,
  scheduledAt: Option[Instant],
  publishedAt: Option[Instant],
  photo: PhotoViewEntity
)

object CardOfDayPhotoEntity {
  inline def from(cardOfDayEntity: CardOfDayEntity, photoEntity: PhotoEntity, photoObjectEntity: PhotoObjectEntity): CardOfDayPhotoEntity =
    CardOfDayPhotoEntity(
      cardOfDayId = cardOfDayEntity.id,
      cardId = cardOfDayEntity.cardId,
      spreadId = cardOfDayEntity.spreadId,
      title = cardOfDayEntity.title,
      description = cardOfDayEntity.description,
      status = cardOfDayEntity.status,
      createdAt = cardOfDayEntity.createdAt,
      scheduledAt = cardOfDayEntity.scheduledAt,
      publishedAt = cardOfDayEntity.publishedAt,
      photo = PhotoViewEntity(
        id = photoEntity.id,
        sourceType = photoEntity.sourceType,
        sourceId = photoEntity.sourceId,
        fileId = photoObjectEntity.fileId,
        hash = photoObjectEntity.hash,
        storageType = photoObjectEntity.storageType,
        path = photoObjectEntity.path,
        bucket = photoObjectEntity.bucket,
        key = photoObjectEntity.key
      )
    )

  def toDomain(cardOfDayPhotoEntity: CardOfDayPhotoEntity): ZIO[Any, TarotError, CardOfDay] =
    for {
      photo <- PhotoViewEntity.toDomain(cardOfDayPhotoEntity.photo)
      cardOfDay = CardOfDay(
        id = CardOfDayId(cardOfDayPhotoEntity.cardOfDayId),
        cardId = CardId(cardOfDayPhotoEntity.cardId),
        spreadId = SpreadId(cardOfDayPhotoEntity.spreadId),
        title = cardOfDayPhotoEntity.title,
        description = cardOfDayPhotoEntity.description,
        status = cardOfDayPhotoEntity.status,
        photo = photo,
        createdAt = cardOfDayPhotoEntity.createdAt,
        scheduledAt = cardOfDayPhotoEntity.scheduledAt,
        publishedAt = cardOfDayPhotoEntity.publishedAt
      )
    } yield cardOfDay
}
