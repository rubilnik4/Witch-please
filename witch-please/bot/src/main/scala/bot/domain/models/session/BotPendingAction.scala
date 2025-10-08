package bot.domain.models.session

enum BotPendingAction:
  case ProjectName
  case SpreadTitle
  case SpreadCardCount(title: String)
  case SpreadPhotoCover(title: String, cardCount: Int)
  case CardPhotoCover(description: String, index: Int)
