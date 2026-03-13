package bot.domain.models.session.pending

import bot.domain.models.session.{CardMode, CardOfDayMode, SpreadMode}

final case class CardOfDayPending(
  mode: CardOfDayMode,
  draft: CardOfDayDraft
)
