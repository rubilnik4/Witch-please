package tarot.application.commands

import tarot.domain.models.cards.ExternalCard
import tarot.domain.models.spreads.{ExternalSpread, Spread}

case class CardCreateCommand(externalCard: ExternalCard)
