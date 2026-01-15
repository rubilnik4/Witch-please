package bot.domain.models.session

import java.util.UUID

enum CardOfDayMode {
  case Create
  case Edit(cardOfDayId: UUID)
}
