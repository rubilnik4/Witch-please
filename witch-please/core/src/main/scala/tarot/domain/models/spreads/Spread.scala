package tarot.domain.models.spreads

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileSource
import shared.models.tarot.photo.PhotoOwnerType
import shared.models.tarot.spreads.SpreadStatus
import tarot.domain.models.photo.{ExternalPhoto, Photo}
import tarot.domain.models.projects.ProjectId
import zio.UIO

import java.time.Instant
import java.util.UUID

final case class Spread(
  id: SpreadId,
  projectId: ProjectId,
  title: String,
  cardCount: Int,
  spreadStatus: SpreadStatus,
  coverPhoto: Photo,
  createdAt: Instant,
  scheduledAt: Option[Instant],
  publishedAt: Option[Instant]
)
{
  override def toString: String = id.toString
}

object Spread {
  def toDomain(externalSpread: ExternalSpread, storedPhoto: FileSource): UIO[Spread] =
    val id = UUID.randomUUID()
    val externalPhotoId = ExternalPhoto.getFileId(externalSpread.coverPhoto)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      spread = Spread(
        id = SpreadId(id),
        projectId = externalSpread.projectId,
        title = externalSpread.title,
        cardCount = externalSpread.cardCount,
        spreadStatus = SpreadStatus.Draft,
        coverPhoto = Photo.toPhoto(storedPhoto, PhotoOwnerType.Spread, id, externalPhotoId),
        createdAt = createdAt,
        scheduledAt = None,
        publishedAt = None)
    } yield spread
}