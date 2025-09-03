package tarot.application.commands.users

import tarot.application.commands.CommandHandler
import tarot.application.commands.users.UserCreateCommand
import tarot.domain.models.authorize.UserId

trait UserCreateCommandHandler extends CommandHandler[UserCreateCommand, UserId]
