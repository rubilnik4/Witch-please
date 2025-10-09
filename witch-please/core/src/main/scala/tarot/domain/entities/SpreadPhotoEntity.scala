package tarot.domain.entities

import tarot.domain.models.TarotError
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId}
import zio.ZIO

import java.util.UUID

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
        cardCount = spreadPhoto.spread.cardCount,
        spreadStatus = spreadPhoto.spread.spreadStatus,
        photo = photo,
        createdAt = spreadPhoto.spread.createdAt,
        scheduledAt = spreadPhoto.spread.scheduledAt,
        publishedAt = spreadPhoto.spread.publishedAt)
    } yield spread
  }
}