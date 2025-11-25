package bot.domain.models.session

enum BotPendingAction:
  case SpreadTitle
  case SpreadCardCount(title: String)
  case SpreadPhoto(title: String, cardCount: Int)
  case CardDescription(index: Int)
  case CardPhoto(index: Int, description: String)
