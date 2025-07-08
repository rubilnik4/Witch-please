package tarot.application.commands

import tarot.domain.models.cards.ExternalCard


case class CardCreateCommand(externalCard: ExternalCard)
