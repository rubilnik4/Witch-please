package tarot.domain.models.spreads

import shared.models.tarot.spreads.SpreadStatus
import java.time.Instant

enum SpreadStatusUpdate:
  case Error(spreadId: SpreadId)
  case Published(spreadId: SpreadId, publishedAt: Instant)
