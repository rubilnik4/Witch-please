package tarot.application.handlers

import tarot.application.commands.SpreadCommand
import tarot.domain.models.contracts.SpreadId

trait SpreadCommandHandler extends CommandHandler[SpreadCommand, SpreadId]
