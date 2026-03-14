package bot.domain.models.session.pending

enum ChannelDraft:
  case Start
  case AwaitingChannelId
  case Complete(channelId: Long, name: String)
