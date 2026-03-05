package bot.domain.models.session

import shared.api.dto.tarot.photo.PhotoRequest
import shared.api.dto.tarot.spreads.*
import shared.models.files.FileSourceType

final case class SpreadSnapshot(
  title: String,
  cardsCount: Int,
  description: String,
  photoSourceId: String                             
)

object SpreadSnapshot {
  def toSnapShot(spread: SpreadResponse): SpreadSnapshot =
    SpreadSnapshot(
      title = spread.title,
      cardsCount = spread.cardsCount,
      description = spread.description,
      photoSourceId = spread.photo.sourceId
    )

  def toCreateRequest(snapshot: SpreadSnapshot): SpreadCreateRequest =
    SpreadCreateRequest(
      title = snapshot.title,
      cardsCount = snapshot.cardsCount,
      description = snapshot.description,
      photo = PhotoRequest(FileSourceType.Telegram, snapshot.photoSourceId)
    )

  def toUpdateRequest(snapshot: SpreadSnapshot): SpreadUpdateRequest =
    SpreadUpdateRequest(
      title = snapshot.title,
      cardsCount = snapshot.cardsCount,
      description = snapshot.description,
      photo = PhotoRequest(FileSourceType.Telegram, snapshot.photoSourceId)
    )
}
