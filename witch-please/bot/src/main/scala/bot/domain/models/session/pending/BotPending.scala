package bot.domain.models.session.pending

enum BotPending:
  case Channel(pending: ChannelPending)
  case Spread(pending: SpreadPending)
  case Card(pending: CardPending)
  case CardOfDay(pending: CardOfDayPending)


