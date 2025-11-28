package bot.domain.models.session

import java.util.UUID

enum SpreadMode {
  case Create
  case Edit(spreadId: UUID)
}
