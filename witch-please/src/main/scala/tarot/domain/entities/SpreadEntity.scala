package tarot.domain.entities

import io.getquill.MappedEncoding
import tarot.domain.models.TarotError
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatus}
import zio.ZIO

import java.time.Instant
import java.util.UUID

final case class SpreadEntity(
    id: UUID,
    title: String,
    cardCount: Int,
    spreadStatus: SpreadStatus,
    coverPhotoId: UUID,
    createdAt: Instant,
    scheduledAt: Option[Instant],
    publishedAt: Option[Instant]
)

final case class SpreadPhotoEntity(
   spread: SpreadEntity,
   coverPhoto: PhotoEntity
)

object SpreadMapper {
  def toDomain(spreadPhoto: SpreadPhotoEntity): ZIO[Any, TarotError, Spread] = {
    for {
      coverPhoto <- PhotoSourceMapper.toDomain(spreadPhoto.coverPhoto)
      spread = Spread(
        id = SpreadId(spreadPhoto.spread.id),
        title = spreadPhoto.spread.title,
        cardCount = spreadPhoto.spread.cardCount,
        spreadStatus = spreadPhoto.spread.spreadStatus,
        coverPhoto = coverPhoto,
        createdAt = spreadPhoto.spread.createdAt,
        scheduledAt = spreadPhoto.spread.scheduledAt,
        publishedAt = spreadPhoto.spread.publishedAt)
    } yield spread
  }

  def toEntity(spread: Spread, coverPhotoId: UUID): SpreadEntity =
    SpreadEntity(
      id = spread.id.id,
      title = spread.title,
      cardCount = spread.cardCount,
      spreadStatus = spread.spreadStatus,
      coverPhotoId = coverPhotoId,
      createdAt = spread.createdAt,
      scheduledAt = spread.scheduledAt,
      publishedAt = spread.publishedAt
    )
}
