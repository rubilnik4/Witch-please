package tarot.application.commands.cardsOfDay.commands

import shared.models.photo.PhotoSource
import tarot.domain.models.cards.CardId
import tarot.domain.models.spreads.SpreadId

import java.util.UUID

final case class CreateCardOfDayCommand(
  cardId: CardId,
  spreadId: SpreadId,
  title: String,
  description: String,
  photo: PhotoSource
)
