package bot.domain.models.session

enum BotPendingAction:
  case SpreadTitle(spreadMode: SpreadMode)
  case SpreadCardsCount(spreadMode: SpreadMode, title: String)
  case SpreadDescription(spreadMode: SpreadMode, title: String, cardCount: Int)
  case SpreadPhoto(spreadMode: SpreadMode, title: String, cardCount: Int, description: String)
  case CardTitle(cardMode: CardMode)
  case CardPhoto(cardMode: CardMode, title: String)
