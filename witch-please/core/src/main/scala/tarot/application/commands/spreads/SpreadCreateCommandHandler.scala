package tarot.application.commands.spreads

import tarot.application.commands.CommandHandler
import tarot.application.commands.spreads.SpreadCreateCommand
import tarot.domain.models.spreads.SpreadId

trait SpreadCreateCommandHandler extends CommandHandler[SpreadCreateCommand, SpreadId]
