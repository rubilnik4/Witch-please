package tarot.domain.entities

import tarot.domain.models.TarotError
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.spreads.SpreadId
import zio.ZIO

import java.util.UUID

final case class CardPhotoEntity(
  card: CardEntity,
  coverPhoto: PhotoEntity
)

object CardPhotoEntity {
  def toDomain(cardPhoto: CardPhotoEntity): ZIO[Any, TarotError, Card] =
    for {
      coverPhoto <- PhotoEntity.toDomain(cardPhoto.coverPhoto)
      card = Card(
        id = CardId(cardPhoto.card.id),
        position = cardPhoto.card.position,
        spreadId = SpreadId(cardPhoto.card.spreadId),
        title = cardPhoto.card.title,
        photo = coverPhoto,
        createdAt = cardPhoto.card.createdAt)
    } yield card
}