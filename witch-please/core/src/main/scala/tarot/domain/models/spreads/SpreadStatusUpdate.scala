package tarot.domain.models.spreads

import shared.models.tarot.spreads.SpreadStatus
import tarot.domain.models.cardsOfDay.CardOfDayId

import java.time.Instant

enum SpreadStatusUpdate:
  case Scheduled(spreadId: SpreadId, scheduledAt: Instant, cardOfDayId: CardOfDayId, cardOfDayAt: Instant)
  case Published(spreadId: SpreadId, publishedAt: Instant)