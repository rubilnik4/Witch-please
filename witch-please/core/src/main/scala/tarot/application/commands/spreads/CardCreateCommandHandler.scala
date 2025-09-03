package tarot.application.commands.spreads

import tarot.application.commands.CommandHandler
import tarot.application.commands.spreads.CardCreateCommand
import tarot.domain.models.cards.CardId

trait CardCreateCommandHandler extends CommandHandler[CardCreateCommand, CardId]
