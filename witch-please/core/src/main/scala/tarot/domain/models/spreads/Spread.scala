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
  cardCount: Int,
  spreadStatus: SpreadStatus,
  photo: Photo,
  createdAt: Instant,
  scheduledAt: Option[Instant],
  cardOfDayDelay: Option[Duration],
  publishedAt: Option[Instant]
)

object Spread {
  def toDomain(command: CreateSpreadCommand, projectId: ProjectId, storedPhoto: FileStorage): UIO[Spread] =
    val id = UUID.randomUUID()
    val photoFile = command.photo
    val photo = Photo.toPhoto(storedPhoto, PhotoOwnerType.Spread, id, photoFile.sourceType, photoFile.fileId)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      spread = Spread(
        id = SpreadId(id),
        projectId = projectId,
        title = command.title,
        cardCount = command.cardCount,
        spreadStatus = SpreadStatus.Draft,
        photo = photo,
        createdAt = createdAt,
        scheduledAt = None,
        cardOfDayDelay = None,
        publishedAt = None)
    } yield spread

  def getCardOfDayAt(scheduledAt: Option[Instant], cardOfDayDelay: Option[Duration]): Option[Instant] =
    for {
      scheduled <- scheduledAt
      delay <- cardOfDayDelay
    } yield scheduled.plus(delay)

  def getCardOfDayDelay(scheduledAt: Option[Instant], cardOfDayAt: Option[Instant]): Option[Duration] =
    for {
      scheduled <- scheduledAt
      cardOfDay <- cardOfDayAt
    } yield Duration.between(scheduled, cardOfDay)
}