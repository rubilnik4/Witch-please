package bot.domain.models.session

import java.util.UUID

enum BotPendingAction:
  case SpreadTitle(spreadMode: SpreadMode)
  case SpreadCardsCount(spreadMode: SpreadMode, title: String)
  case SpreadDescription(spreadMode: SpreadMode, title: String, cardCount: Int)
  case SpreadPhoto(spreadMode: SpreadMode, title: String, cardCount: Int, description: String)
  
  case CardTitle(cardMode: CardMode)
  case CardDescription(cardMode: CardMode, title: String)
  case CardPhoto(cardMode: CardMode, title: String, description: String)

  case CardOfDayCardId(cardMode: CardOfDayMode)
  case CardOfDayDescription(cardMode: CardOfDayMode, cardId: UUID)
  case CardOfDayPhoto(cardMode: CardOfDayMode, cardId: UUID, description: String)
