package bot.domain.models.session.pending

import java.util.UUID

enum CardOfDayDraft {
  case Start
  case AwaitingCardId
  case AwaitingTitle(cardId: UUID)
  case AwaitingDescription(cardId: UUID, title: String)
  case AwaitingPhoto(cardId: UUID, title: String, description: String)
  case Complete(cardId: UUID, title: String, description: String, photoSourceId: String)
}