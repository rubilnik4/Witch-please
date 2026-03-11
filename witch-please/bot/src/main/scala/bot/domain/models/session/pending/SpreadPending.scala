package bot.domain.models.session.pending

import bot.domain.models.session.SpreadMode

final case class SpreadPending(
  mode: SpreadMode,
  draft: SpreadDraft
)
