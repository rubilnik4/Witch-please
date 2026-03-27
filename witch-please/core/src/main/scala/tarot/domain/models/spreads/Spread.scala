package tarot.domain.models.spreads

import shared.infrastructure.services.common.DateTimeService
import shared.models.photo.PhotoFile
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
  def toDomain(command: CreateSpreadCommand, projectId: ProjectId, photoFile: PhotoFile): UIO[Spread] =
    val photo = Photo.create(photoFile, command.photo.sourceType, command.photo.sourceId)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      spread = Spread(
        id = SpreadId(UUID.randomUUID()),
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

  def clone(spread: Spread, photoFile: PhotoFile): UIO[Spread] =
    val id = UUID.randomUUID()
    val photo = Photo.create(photoFile, spread.photo.sourceType, spread.photo.sourceId)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      cloneSpread = Spread(
        id = SpreadId(id),
        projectId = spread.projectId,
        title = spread.title,
        cardsCount = spread.cardsCount,
        description = spread.description,
        status = SpreadStatus.Draft,
        photo = photo,
        createdAt = createdAt,
        scheduledAt = None,
        publishedAt = None)
    } yield cloneSpread  
}
