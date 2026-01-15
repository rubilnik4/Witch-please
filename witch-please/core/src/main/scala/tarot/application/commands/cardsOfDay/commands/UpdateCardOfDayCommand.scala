package tarot.application.commands.cardsOfDay.commands

import tarot.domain.models.cardsOfDay.CardOfDayId
import tarot.domain.models.cards.CardId
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.SpreadId

import java.util.UUID

final case class UpdateCardOfDayCommand(
  cardOfDayId: CardOfDayId,
  cardId: CardId,
  description: String,
  photo: PhotoSource
)
