package bot.domain.models.session.pending

import bot.domain.models.session.ChannelMode

final case class ChannelPending(
  mode: ChannelMode,
  draft: ChannelDraft
)
