package tarot.domain.models.spreads

import shared.models.tarot.spreads.SpreadStatus
import java.time.Instant

enum SpreadStatusUpdate:
  case Scheduled(spreadId: SpreadId, scheduledAt: Instant, expectedAt: Option[Instant])
  case Published(spreadId: SpreadId, publishedAt: Instant)