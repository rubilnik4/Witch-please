package tarot.domain.entities

import tarot.domain.models.TarotError
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.spreads.SpreadId
import zio.ZIO

import java.time.Instant
import java.util.UUID

final case class CardPhotoEntity(
  cardId: UUID,
  position: Int,
  spreadId: UUID,
  title: String,
  description: String,
  createdAt: Instant,
  photo: PhotoViewEntity
)

object CardPhotoEntity {
  inline def from(cardEntity: CardEntity, photoEntity: PhotoEntity, photoObjectEntity: PhotoObjectEntity): CardPhotoEntity =
    CardPhotoEntity(
      cardId = cardEntity.id,
      position = cardEntity.position,
      spreadId = cardEntity.spreadId,
      title = cardEntity.title,
      description = cardEntity.description,
      createdAt = cardEntity.createdAt,
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

  def toDomain(cardPhotoEntity: CardPhotoEntity): ZIO[Any, TarotError, Card] =
    for {
      photo <- PhotoViewEntity.toDomain(cardPhotoEntity.photo)
      card = Card(
        id = CardId(cardPhotoEntity.cardId),
        position = cardPhotoEntity.position,
        spreadId = SpreadId(cardPhotoEntity.spreadId),
        title = cardPhotoEntity.title,
        description = cardPhotoEntity.description,
        photo = photo,
        createdAt = cardPhotoEntity.createdAt
      )
    } yield card
}
