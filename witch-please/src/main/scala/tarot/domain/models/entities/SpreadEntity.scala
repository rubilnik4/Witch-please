package tarot.domain.models.entities

import tarot.domain.models.spreads.{Spread, SpreadStatus}
import io.getquill.MappedEncoding

import java.time.Instant
import java.util.UUID

final case class SpreadEntity(
    id: UUID,
    title: String,
    cardCount: Integer,
    spreadStatus: SpreadStatus,
    coverPhotoUrl: String,
    time: Instant
)

object SpreadMapper {
  def toDomain(spread: SpreadEntity): Spread =
    Spread(
      id = spread.id,
      title = spread.title,
      cardCount = spread.cardCount,
      spreadStatus = spread.spreadStatus,
      coverPhotoUrl = spread.coverPhotoUrl,
      time = spread.time
    )
    
  def toEntity(spread: Spread): SpreadEntity =
    SpreadEntity(
      id = spread.id,
      title = spread.title,
      cardCount = spread.cardCount,
      spreadStatus = spread.spreadStatus,
      coverPhotoUrl = spread.coverPhotoUrl,
      time = spread.time
    )
}

given encodeStatus: MappedEncoding[SpreadStatus, String] =
  MappedEncoding(_.toString)

given decodeStatus: MappedEncoding[String, SpreadStatus] =
  MappedEncoding(SpreadStatus.valueOf)
