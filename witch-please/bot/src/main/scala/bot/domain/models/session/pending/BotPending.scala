package bot.domain.models.session.pending

import bot.domain.models.session.ChannelMode

enum BotPending:
//  case Channel(action: ChannelPending)
  case Spread(pending: SpreadPending)
  case Card(pending: CardPending)
  case CardOfDay(pending: CardOfDayPending)

  case ChannelChannelId(channelMode: ChannelMode)


