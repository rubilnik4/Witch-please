package tarot.application.handlers.spreads

import tarot.application.commands.spreads.SpreadCreateCommand
import tarot.application.handlers.CommandHandler
import tarot.domain.models.spreads.SpreadId

trait SpreadCreateCommandHandler extends CommandHandler[SpreadCreateCommand, SpreadId]
