package bot.domain.models.session

import shared.models.tarot.cards.CardPosition

final case class SpreadProgress(
  cardsCount: Int,
  createdPositions: Set[CardPosition]
)
