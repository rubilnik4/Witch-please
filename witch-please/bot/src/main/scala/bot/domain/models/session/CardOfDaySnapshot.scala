package bot.domain.models.session

import shared.api.dto.tarot.cards.*
import shared.api.dto.tarot.cardsOfDay.*
import shared.api.dto.tarot.photo.PhotoRequest
import shared.api.dto.tarot.spreads.*
import shared.models.files.FileSourceType

import java.util.UUID

final case class CardOfDaySnapshot(
  cardId: UUID,
  title: String,
  description: String,
  photoSourceId: String                             
)

object CardOfDaySnapshot {
  def toSnapShot(cardOfDay: CardOfDayResponse): CardOfDaySnapshot =
    CardOfDaySnapshot(
      cardId = cardOfDay.cardId,
      title = cardOfDay.title,
      description = cardOfDay.description,
      photoSourceId = cardOfDay.photo.sourceId
    )

  def toCreateRequest(snapshot: CardOfDaySnapshot): CardOfDayCreateRequest =
    CardOfDayCreateRequest(
      cardId = snapshot.cardId,
      title = snapshot.title,
      description = snapshot.description,
      photo = PhotoRequest(FileSourceType.Telegram, snapshot.photoSourceId)
    )

  def toUpdateRequest(snapshot: CardOfDaySnapshot): CardOfDayUpdateRequest =
    CardOfDayUpdateRequest(
      cardId = snapshot.cardId,
      title = snapshot.title,
      description = snapshot.description,
      photo = PhotoRequest(FileSourceType.Telegram, snapshot.photoSourceId)
    )
}
