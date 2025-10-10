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
        index = cardPhoto.card.index,
        spreadId = SpreadId(cardPhoto.card.spreadId),
        description = cardPhoto.card.description,
        photo = coverPhoto,
        createdAt = cardPhoto.card.createdAt)
    } yield card
}