package tarot.application.commands

import tarot.domain.models.contracts.SpreadId
import tarot.domain.models.spreads.{ExternalSpread, Spread}

case class SpreadPublishCommand(spreadId: SpreadId)
