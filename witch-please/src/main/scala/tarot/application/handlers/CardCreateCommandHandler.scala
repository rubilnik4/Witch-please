package tarot.application.handlers

import tarot.application.commands.CardCreateCommand
import tarot.domain.models.cards.CardId

trait CardCreateCommandHandler extends CommandHandler[CardCreateCommand, CardId]
