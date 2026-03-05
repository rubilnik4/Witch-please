package bot.domain.models.session.pending

enum SpreadDraft:
  case Start
  case AwaitingTitle
  case AwaitingCardsCount(title: String)
  case AwaitingDescription(title: String, cardsCount: Int)
  case AwaitingPhoto(title: String, cardsCount: Int, description: String)
  case Complete(title: String, cardsCount: Int, description: String, photoSourceId: String)


