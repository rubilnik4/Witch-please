package tarot.domain.entities

import io.getquill.MappedEncoding
import tarot.domain.models.TarotError
import tarot.domain.models.spreads.{Spread, SpreadStatus}
import zio.ZIO

import java.time.Instant
import java.util.UUID

final case class SpreadEntity(
    id: UUID,
    title: String,
    cardCount: Int,
    spreadStatus: SpreadStatus,
    coverPhotoId: UUID,
    time: Instant
)

final case class SpreadPhotoEntity(
   spread: SpreadEntity,
   coverPhoto: PhotoSourceEntity
)

object SpreadMapper {
  def toDomain(spread: SpreadPhotoEntity): ZIO[Any, TarotError, Spread] =
    PhotoSourceMapper.toDomain(spread.coverPhoto)
      .map(coverPhoto =>
        Spread(
          id = spread.spread.id,
          title = spread.spread.title,
          cardCount = spread.spread.cardCount,
          spreadStatus = spread.spread.spreadStatus,
          coverPhoto = coverPhoto,
          time = spread.spread.time
      ))    
    
  def toEntity(spread: Spread, coverPhotoId: UUID): SpreadEntity =
    SpreadEntity(
      id = spread.id,
      title = spread.title,
      cardCount = spread.cardCount,
      spreadStatus = spread.spreadStatus,
      coverPhotoId = coverPhotoId,
      time = spread.time
    )
}
