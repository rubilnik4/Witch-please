package tarot.domain.entities

import io.getquill.MappedEncoding
import tarot.domain.models.TarotError
import tarot.domain.models.photo.PhotoSource
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

object SpreadMapper {
  def toDomain(spread: SpreadEntity, coverPhoto: PhotoSourceEntity): ZIO[Any, TarotError, Spread] =
    PhotoSourceMapper.toDomain(coverPhoto)
      .map(coverPhoto =>
        Spread(
          id = spread.id,
          title = spread.title,
          cardCount = spread.cardCount,
          spreadStatus = spread.spreadStatus,
          coverPhoto = coverPhoto,
          time = spread.time
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

given encodeStatus: MappedEncoding[SpreadStatus, String] =
  MappedEncoding(_.toString)

given decodeStatus: MappedEncoding[String, SpreadStatus] =
  MappedEncoding(SpreadStatus.valueOf)
