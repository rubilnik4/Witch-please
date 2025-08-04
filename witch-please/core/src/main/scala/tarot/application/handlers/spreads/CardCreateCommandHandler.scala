package tarot.application.handlers.spreads

import tarot.application.commands.spreads.CardCreateCommand
import tarot.application.handlers.CommandHandler
import tarot.domain.models.cards.CardId

trait CardCreateCommandHandler extends CommandHandler[CardCreateCommand, CardId]
