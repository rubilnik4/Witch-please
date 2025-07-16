package tarot.application.handlers.spreads

import tarot.application.commands.spreads.SpreadPublishCommand
import tarot.application.handlers.CommandHandler

trait SpreadPublishCommandHandler extends CommandHandler[SpreadPublishCommand, Unit]
