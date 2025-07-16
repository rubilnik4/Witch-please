package tarot.application.handlers.users

import tarot.application.commands.users.UserCreateCommand
import tarot.application.handlers.CommandHandler
import tarot.domain.models.auth.UserId

trait UserCreateCommandHandler extends CommandHandler[UserCreateCommand, UserId]
