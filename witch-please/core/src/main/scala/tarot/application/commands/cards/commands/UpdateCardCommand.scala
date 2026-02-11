package tarot.application.commands.cards.commands

import shared.models.photo.PhotoSource
import tarot.domain.models.cards.CardId
import tarot.domain.models.spreads.SpreadId

final case class UpdateCardCommand(
  cardId: CardId,
  title: String,
  description: String,
  photo: PhotoSource
)
