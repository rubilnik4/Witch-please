package tarot.domain.entities

import tarot.domain.models.TarotError
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId}
import zio.ZIO

import java.time.Instant
import java.util.UUID

final case class SpreadPhotoEntity(
  spreadId: UUID,
  projectId: UUID,
  title: String,
  cardCount: Int,
  description: String,
  status: shared.models.tarot.spreads.SpreadStatus,
  createdAt: Instant,
  scheduledAt: Option[Instant],
  publishedAt: Option[Instant],
  photo: PhotoViewEntity
)

object SpreadPhotoEntity {
  inline def from(spreadEntity: SpreadEntity, photoEntity: PhotoEntity, photoObjectEntity: PhotoObjectEntity): SpreadPhotoEntity =
    SpreadPhotoEntity(
      spreadId = spreadEntity.id,
      projectId = spreadEntity.projectId,
      title = spreadEntity.title,
      cardCount = spreadEntity.cardCount,
      description = spreadEntity.description,
      status = spreadEntity.status,
      createdAt = spreadEntity.createdAt,
      scheduledAt = spreadEntity.scheduledAt,
      publishedAt = spreadEntity.publishedAt,
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

  def toDomain(spreadPhotoEntity: SpreadPhotoEntity): ZIO[Any, TarotError, Spread] = {
    for {
      photo <- PhotoViewEntity.toDomain(spreadPhotoEntity.photo)
      spread = Spread(
        id = SpreadId(spreadPhotoEntity.spreadId),
        projectId = ProjectId(spreadPhotoEntity.projectId),
        title = spreadPhotoEntity.title,
        cardsCount = spreadPhotoEntity.cardCount,
        description = spreadPhotoEntity.description,
        status = spreadPhotoEntity.status,
        photo = photo,
        createdAt = spreadPhotoEntity.createdAt,
        scheduledAt = spreadPhotoEntity.scheduledAt,
        publishedAt = spreadPhotoEntity.publishedAt)
    } yield spread
  }
}
