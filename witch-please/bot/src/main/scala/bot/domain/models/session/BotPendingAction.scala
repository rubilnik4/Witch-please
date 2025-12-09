package bot.domain.models.session

enum BotPendingAction:
  case SpreadTitle(spreadMode: SpreadMode)
  case SpreadCardCount(spreadMode: SpreadMode, title: String)
  case SpreadPhoto(spreadMode: SpreadMode, title: String, cardCount: Int)
  case CardTitle(cardMode: CardMode)
  case CardPhoto(cardMode: CardMode, title: String)
