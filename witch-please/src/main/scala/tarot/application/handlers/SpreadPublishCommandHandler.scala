package tarot.application.handlers

import tarot.application.commands.{SpreadCreateCommand, SpreadPublishCommand}
import tarot.domain.models.contracts.SpreadId

trait SpreadPublishCommandHandler extends CommandHandler[SpreadPublishCommand, Unit]
