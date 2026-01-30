package bot.domain.models.session

import java.util.UUID

enum ChannelMode {
  case Create
  case Edit(userChannelId: UUID)
}
