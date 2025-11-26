package tarot.application.jobs.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.spreads.SpreadId

final case class SpreadPublishResult(
  id: SpreadId,
  publishType: SpreadPublishType,                                  
  result: Either[TarotError, Unit]
)