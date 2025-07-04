package tarot.application.handlers

import tarot.application.commands.{CardCreateCommand, SpreadCreateCommand}
import tarot.domain.models.contracts.{CardId, SpreadId}

trait CardCreateCommandHandler extends CommandHandler[CardCreateCommand, CardId]
