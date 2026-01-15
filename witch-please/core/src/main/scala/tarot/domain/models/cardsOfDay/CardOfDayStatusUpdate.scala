package tarot.domain.models.cardsOfDay

import shared.models.tarot.cardOfDay.CardOfDayStatus
import shared.models.tarot.spreads.SpreadStatus

import java.time.Instant

enum CardOfDayStatusUpdate: 
  case Published(cardOfDayId: CardOfDayId, publishedAt: Instant)