package tarot.application.jobs.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.spreads.SpreadId

final case class SpreadPublishResult(
  id: SpreadId,
  result: Either[TarotError, Unit]
)