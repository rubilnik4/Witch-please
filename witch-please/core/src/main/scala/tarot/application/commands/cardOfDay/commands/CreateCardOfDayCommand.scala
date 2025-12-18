package tarot.application.commands.cardOfDay.commands

import tarot.domain.models.cards.CardId
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.SpreadId

import java.util.UUID

final case class CreateCardOfDayCommand(
  cardId: CardId,
  spreadId: SpreadId,
  description: String,
  photo: PhotoSource
)
