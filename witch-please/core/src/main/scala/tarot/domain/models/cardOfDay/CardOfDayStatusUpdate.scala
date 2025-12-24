package tarot.domain.models.cardOfDay

import shared.models.tarot.cardOfDay.CardOfDayStatus
import shared.models.tarot.spreads.SpreadStatus

import java.time.Instant

enum CardOfDayStatusUpdate: 
  case Published(cardOfDayId: CardOfDayId, publishedAt: Instant)