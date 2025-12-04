package tarot.application.commands.cards.commands

import tarot.domain.models.cards.CardId
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.SpreadId

final case class UpdateCardCommand(
  cardId: CardId,
  title: String,
  photo: PhotoSource
)
