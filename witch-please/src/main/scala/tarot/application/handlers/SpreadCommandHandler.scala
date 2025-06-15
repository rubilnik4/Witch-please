package tarot.application.handlers

import tarot.application.commands.SpreadCommand

trait SpreadCommandHandler extends CommandHandler[SpreadCommand, String]
