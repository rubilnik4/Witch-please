package tarot.domain.models.cardOfDay

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileStorage
import shared.models.tarot.cardOfDay.CardOfDayStatus
import shared.models.tarot.photo.PhotoOwnerType
import tarot.application.commands.cardOfDay.commands.CreateCardOfDayCommand
import tarot.application.commands.cards.commands.CreateCardCommand
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.photo.Photo
import tarot.domain.models.spreads.SpreadId
import zio.UIO

import java.time.{Duration, Instant}
import java.util.UUID

final case class CardOfDay(
  id: CardOfDayId,                        
  cardId: CardId,
  spreadId: SpreadId,                        
  description: String,
  status: CardOfDayStatus,
  photo: Photo,
  createdAt: Instant,
  scheduledAt: Option[Instant],
  publishedAt: Option[Instant]
)

object CardOfDay {
  def toDomain(command: CreateCardOfDayCommand, photoFile: FileStorage): UIO[CardOfDay] =
    val id = UUID.randomUUID()
    val photoSource = command.photo
    val photo = Photo.toPhoto(UUID.randomUUID(), photoFile, PhotoOwnerType.Card, id, photoSource.sourceType, photoSource.sourceId)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      cardOfDay = CardOfDay(
        id = CardOfDayId(id),
        cardId = command.cardId,
        spreadId = command.spreadId,
        description = command.description,
        status = CardOfDayStatus.Draft,
        photo = photo,
        createdAt = createdAt,
        scheduledAt = None,
        publishedAt = None)
    } yield cardOfDay

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
