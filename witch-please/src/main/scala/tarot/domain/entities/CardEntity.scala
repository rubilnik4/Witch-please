package tarot.domain.entities

import io.getquill.MappedEncoding
import tarot.domain.models.TarotError
import tarot.domain.models.cards.Card
import tarot.domain.models.contracts.SpreadId
import tarot.domain.models.spreads.{Spread, SpreadStatus}
import zio.ZIO

import java.time.Instant
import java.util.UUID

final case class CardEntity(
    id: UUID,
    spreadId: UUID,
    description: String,
    coverPhotoId: UUID,
    time: Instant
)

final case class CardPhotoEntity(
   card: CardEntity,
   coverPhoto: PhotoEntity
)

object CardMapper {
  def toDomain(card: CardPhotoEntity): ZIO[Any, TarotError, Card] =
    PhotoSourceMapper.toDomain(card.coverPhoto)
      .map(coverPhoto =>
        Card(
          id = card.card.id,
          spreadId = SpreadId(card.card.spreadId),
          description = card.card.description,
          coverPhoto = coverPhoto,
          time = card.card.time
      ))    
    
  def toEntity(card: Card, coverPhotoId: UUID): CardEntity =
    CardEntity(
      id = card.id,
      spreadId = card.spreadId.id,
      description = card.description,
      coverPhotoId = coverPhotoId,
      time = card.time
    )
}
