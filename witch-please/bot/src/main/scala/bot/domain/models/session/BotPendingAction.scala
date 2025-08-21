package bot.domain.models.session

enum BotPendingAction:
  case SpreadCover(title: String, cardCount: Int)
