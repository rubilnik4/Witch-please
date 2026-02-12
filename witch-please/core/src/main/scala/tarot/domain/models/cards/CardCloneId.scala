package tarot.domain.models.cards

import java.util.UUID

final case class CardCloneId(
  cardId: CardId,
  originalCardId: CardId
)

