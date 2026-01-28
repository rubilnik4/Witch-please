package tarot.domain.entities

import tarot.domain.models.TarotError
import tarot.domain.models.cardsOfDay.{CardOfDay, CardOfDayId}
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.spreads.SpreadId
import zio.ZIO

import java.time.Instant
import java.util.UUID

final case class CardOfDayPhotoEntity(
  cardOfDay: CardOfDayEntity,
  photo: PhotoEntity
)

object CardOfDayPhotoEntity {
  def toDomain(cardOfDayPhoto: CardOfDayPhotoEntity): ZIO[Any, TarotError, CardOfDay] =
    for {
      photo <- PhotoEntity.toDomain(cardOfDayPhoto.photo)
      card = CardOfDay(
        id = CardOfDayId(cardOfDayPhoto.cardOfDay.id),
        cardId = CardId(cardOfDayPhoto.cardOfDay.cardId),
        spreadId = SpreadId(cardOfDayPhoto.cardOfDay.spreadId),
        title = cardOfDayPhoto.cardOfDay.title,
        description = cardOfDayPhoto.cardOfDay.description,
        status = cardOfDayPhoto.cardOfDay.status,
        photo = photo,
        createdAt = cardOfDayPhoto.cardOfDay.createdAt,
        scheduledAt = cardOfDayPhoto.cardOfDay.scheduledAt,
        publishedAt = cardOfDayPhoto.cardOfDay.publishedAt)
    } yield card
}