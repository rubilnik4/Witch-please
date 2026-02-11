package tarot.domain.models.cardsOfDay

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileStored
import shared.models.tarot.cardOfDay.CardOfDayStatus
import shared.models.tarot.photo.PhotoOwnerType
import tarot.application.commands.cardsOfDay.commands.CreateCardOfDayCommand
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
  title: String,
  description: String,
  status: CardOfDayStatus,
  photo: Photo,
  createdAt: Instant,
  scheduledAt: Option[Instant],
  publishedAt: Option[Instant]
)

object CardOfDay {
  def toDomain(command: CreateCardOfDayCommand, photoFile: FileStored): UIO[CardOfDay] =
    val id = UUID.randomUUID()
    val photoSource = command.photo
    val photo = Photo.toPhoto(UUID.randomUUID(), photoFile, PhotoOwnerType.CardOfDay, id, photoSource.sourceType, photoSource.sourceId)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      cardOfDay = CardOfDay(
        id = CardOfDayId(id),
        cardId = command.cardId,
        spreadId = command.spreadId,
        title = command.title,
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

  def clone(cardOfDay: CardOfDay, spreadId: SpreadId, photoFile: FileStored): UIO[CardOfDay] =
    val id = UUID.randomUUID()
    val photo = Photo.toPhoto(UUID.randomUUID(), photoFile, PhotoOwnerType.CardOfDay, id, cardOfDay.photo.sourceType, cardOfDay.photo.sourceId)
    for {
      createdAt <- DateTimeService.getDateTimeNow
      cloneCardOfDay = CardOfDay(
        id = CardOfDayId(id),
        cardId = cardOfDay.cardId,        
        spreadId = spreadId,
        title = cardOfDay.title,
        description = cardOfDay.description,
        status = CardOfDayStatus.Draft,
        photo = photo,
        createdAt = createdAt,
        scheduledAt = None,
        publishedAt = None)
    } yield cloneCardOfDay  
}
