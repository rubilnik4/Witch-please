package bot.domain.models.session.pending

import bot.domain.models.session.{CardMode, SpreadMode}

final case class CardPending(
  mode: CardMode,
  draft: CardDraft
)
