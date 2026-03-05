package bot.domain.models.session.pending

import bot.domain.models.session.{CardMode, CardOfDayMode, ChannelMode}

import java.util.UUID

enum BotPending:
//  case Channel(action: ChannelPending)
  case Spread(pending: SpreadPending)
//  case Card(action: CardPending)
//  case CardOfDay(action: CardOfDayPending)

  case ChannelChannelId(channelMode: ChannelMode)

  case CardTitle(cardMode: CardMode)
  case CardDescription(cardMode: CardMode, title: String)
  case CardPhoto(cardMode: CardMode, title: String, description: String)

  case CardOfDayCardId(cardMode: CardOfDayMode)
  case CardOfDayTitle(cardMode: CardOfDayMode, cardId: UUID)
  case CardOfDayDescription(cardMode: CardOfDayMode, cardId: UUID, title: String)
  case CardOfDayPhoto(cardMode: CardOfDayMode, cardId: UUID, title: String, description: String)



