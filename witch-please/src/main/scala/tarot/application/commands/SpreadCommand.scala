package tarot.application.commands

import tarot.domain.models.spreads.{ExternalSpread, Spread}

case class SpreadCommand(externalSpread: ExternalSpread)
