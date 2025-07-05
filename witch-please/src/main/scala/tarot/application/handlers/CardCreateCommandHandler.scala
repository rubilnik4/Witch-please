package tarot.application.handlers

import tarot.application.commands.{CardCreateCommand, SpreadCreateCommand}
import tarot.domain.models.cards.CardId
import tarot.domain.models.spreads.SpreadId

trait CardCreateCommandHandler extends CommandHandler[CardCreateCommand, CardId]
