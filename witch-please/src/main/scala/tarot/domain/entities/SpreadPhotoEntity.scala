package tarot.domain.entities

import tarot.domain.models.TarotError
import tarot.domain.models.spreads.{Spread, SpreadId}
import zio.ZIO

import java.util.UUID

final case class SpreadPhotoEntity(
  spread: SpreadEntity,
  coverPhoto: PhotoEntity
)

object SpreadPhotoEntity {
  def toDomain(spreadPhoto: SpreadPhotoEntity): ZIO[Any, TarotError, Spread] = {
    for {
      coverPhoto <- PhotoEntity.toDomain(spreadPhoto.coverPhoto)
      spread = Spread(
        id = SpreadId(spreadPhoto.spread.id),
        title = spreadPhoto.spread.title,
        cardCount = spreadPhoto.spread.cardCount,
        spreadStatus = spreadPhoto.spread.spreadStatus,
        coverPhoto = coverPhoto,
        createdAt = spreadPhoto.spread.createdAt,
        scheduledAt = spreadPhoto.spread.scheduledAt,
        publishedAt = spreadPhoto.spread.publishedAt)
    } yield spread
  }
}