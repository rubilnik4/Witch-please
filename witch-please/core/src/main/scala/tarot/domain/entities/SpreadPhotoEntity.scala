package tarot.domain.entities

import tarot.domain.models.TarotError
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId}
import zio.ZIO

final case class SpreadPhotoEntity(
  spread: SpreadEntity,
  photo: PhotoEntity
)

object SpreadPhotoEntity {
  def toDomain(spreadPhoto: SpreadPhotoEntity): ZIO[Any, TarotError, Spread] = {
    for {
      photo <- PhotoEntity.toDomain(spreadPhoto.photo)
      spread = Spread(
        id = SpreadId(spreadPhoto.spread.id),
        projectId = ProjectId(spreadPhoto.spread.projectId),
        title = spreadPhoto.spread.title,
        cardsCount = spreadPhoto.spread.cardCount,
        description = spreadPhoto.spread.description,
        status = spreadPhoto.spread.status,
        photo = photo,
        createdAt = spreadPhoto.spread.createdAt,
        scheduledAt = spreadPhoto.spread.scheduledAt,
        publishedAt = spreadPhoto.spread.publishedAt)
    } yield spread
  }
}