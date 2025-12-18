package tarot.domain.models.spreads

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileStorage
import shared.models.tarot.photo.PhotoOwnerType
import shared.models.tarot.spreads.SpreadStatus
import tarot.application.commands.spreads.commands.CreateSpreadCommand
import tarot.domain.models.photo.Photo
import tarot.domain.models.projects.ProjectId
import zio.UIO

import java.time.*
import java.util.UUID

final case class Spread(
  id: SpreadId,
  projectId: ProjectId,
  title: String,
  cardsCount: Int,
  description: String,
  status: SpreadStatus,
  photo: Photo,
  createdAt: Instant,
  scheduledAt: Option[Instant],
  publishedAt: Option[Instant]
)

object Spread {
  def toDomain(command: CreateSpreadCommand, projectId: ProjectId, photoFile: FileStorage): UIO[Spread] =
    val id = UUID.randomUUID()
    val photoSource = command.photo
    val photo = Photo.toPhoto(UUID.randomUUID(), photoFile, PhotoOwnerType.Spread, id, photoSource.sourceType, photoSource.sourceId)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      spread = Spread(
        id = SpreadId(id),
        projectId = projectId,
        title = command.title,
        cardsCount = command.cardsCount,
        description = command.description,
        status = SpreadStatus.Draft,
        photo = photo,
        createdAt = createdAt,
        scheduledAt = None,
        publishedAt = None)
    } yield spread
}