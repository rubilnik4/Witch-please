package tarot.domain.entities

import io.getquill.MappedEncoding
import tarot.domain.models.TarotError
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatus}
import zio.ZIO

import java.time.Instant
import java.util.UUID

final case class CardEntity(
                             id: UUID,
                             spreadId: UUID,
                             description: String,
                             coverPhotoId: UUID,
                             createdAt: Instant
)

final case class CardPhotoEntity(
   card: CardEntity,
   coverPhoto: PhotoEntity
)

object CardMapper {
  def toDomain(cardPhoto: CardPhotoEntity): ZIO[Any, TarotError, Card] =
    for {
      coverPhoto <- PhotoSourceMapper.toDomain(cardPhoto.coverPhoto)
      card = Card(
        id = CardId(cardPhoto.card.id),
        spreadId = SpreadId(cardPhoto.card.spreadId),
        description = cardPhoto.card.description,
        coverPhoto = coverPhoto,
        createdAt = cardPhoto.card.createdAt)
    } yield card
      
  def toEntity(card: Card, coverPhotoId: UUID): CardEntity =
    CardEntity(
      id = card.id.id,
      spreadId = card.spreadId.id,
      description = card.description,
      coverPhotoId = coverPhotoId,
      createdAt = card.createdAt
    )
}
