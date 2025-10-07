package bot.domain.models.session

enum BotPendingAction:
  case ProjectName
  case SpreadTitle
  case SpreadCardCount(title: String)
  case SpreadCover(title: String, cardCount: Int)
  case CardCover(description: String, index: Int)
