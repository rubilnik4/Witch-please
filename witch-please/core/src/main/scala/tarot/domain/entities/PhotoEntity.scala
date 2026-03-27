package tarot.domain.entities

import shared.models.files.*
import tarot.domain.models.TarotError
import tarot.domain.models.photo.{Photo, PhotoId}
import zio.ZIO

import java.util.UUID

final case class PhotoEntity(
  id: UUID,
  photoObjectId: UUID,
  sourceType: FileSourceType,
  sourceId: String
)

object PhotoEntity {
  def toDomain(photoEntity: PhotoEntity, photoObjectEntity: PhotoObjectEntity): ZIO[Any, TarotError, Photo] =
    for {
      photoObject <- PhotoObjectEntity.toDomain(photoObjectEntity)
    } yield Photo(
        id = PhotoId(photoEntity.id),
        photoObject = photoObject,
        sourceType = photoEntity.sourceType,
        sourceId = photoEntity.sourceId
      )

  def toEntity(photo: Photo, photoObjectId: UUID): PhotoEntity =
    PhotoEntity(
      id = photo.id.id,
      photoObjectId = photoObjectId,
      sourceType = photo.sourceType,
      sourceId = photo.sourceId
    )
}
