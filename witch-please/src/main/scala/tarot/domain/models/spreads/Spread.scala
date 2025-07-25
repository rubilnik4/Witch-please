package tarot.domain.models.spreads

import tarot.domain.models.photo.{Photo, PhotoOwnerType, PhotoSource}
import tarot.domain.models.projects.ProjectId
import tarot.infrastructure.services.common.DateTimeService
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
  def toDomain(externalSpread: ExternalSpread, storedPhoto: PhotoSource): UIO[Spread] =
    val id = UUID.randomUUID()
    for {
      createdAt <- DateTimeService.getDateTimeNow
      spread = Spread(
        id = SpreadId(id),
        projectId = externalSpread.projectId,
        title = externalSpread.title,
        cardCount = externalSpread.cardCount,
        spreadStatus = SpreadStatus.Draft,
        coverPhoto = Photo.toPhotoSource(storedPhoto, PhotoOwnerType.Spread, id),
        createdAt = createdAt,
        scheduledAt = None,
        publishedAt = None)
    } yield spread
}