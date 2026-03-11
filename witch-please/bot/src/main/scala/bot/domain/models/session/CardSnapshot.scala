package bot.domain.models.session

import shared.api.dto.tarot.cards.*
import shared.api.dto.tarot.photo.PhotoRequest
import shared.api.dto.tarot.spreads.*
import shared.models.files.FileSourceType

final case class CardSnapshot(
  title: String,
  description: String,
  photoSourceId: String                             
)

object CardSnapshot {
  def toSnapShot(card: CardResponse): CardSnapshot =
    CardSnapshot(
      title = card.title,
      description = card.description,
      photoSourceId = card.photo.sourceId
    )

  def toCreateRequest(position: Int, snapshot: CardSnapshot): CardCreateRequest =
    CardCreateRequest(
      position = position,
      title = snapshot.title,
      description = snapshot.description,
      photo = PhotoRequest(FileSourceType.Telegram, snapshot.photoSourceId)
    )

  def toUpdateRequest(snapshot: CardSnapshot): CardUpdateRequest =
    CardUpdateRequest(
      title = snapshot.title,
      description = snapshot.description,
      photo = PhotoRequest(FileSourceType.Telegram, snapshot.photoSourceId)
    )
}
