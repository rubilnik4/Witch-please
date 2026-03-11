package bot.domain.models.session.pending

enum CardDraft {
  case Start
  case AwaitingTitle
  case AwaitingDescription(title: String)
  case AwaitingPhoto(title: String, description: String)
  case Complete(title: String, description: String, photoSourceId: String)
}