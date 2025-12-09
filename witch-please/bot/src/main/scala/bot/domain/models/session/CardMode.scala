package bot.domain.models.session

import java.util.UUID

enum CardMode {
  case Create(position: Int)
  case Edit(cardId: UUID)
}
