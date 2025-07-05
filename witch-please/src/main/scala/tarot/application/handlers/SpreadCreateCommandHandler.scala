package tarot.application.handlers

import tarot.application.commands.SpreadCreateCommand
import tarot.domain.models.spreads.SpreadId

trait SpreadCreateCommandHandler extends CommandHandler[SpreadCreateCommand, SpreadId]
