package tarot.application.handlers

import tarot.application.commands.{CardCommand, SpreadCommand}
import tarot.domain.models.contracts.{CardId, SpreadId}

trait CardCommandHandler extends CommandHandler[CardCommand, CardId]
