package tarot.application.handlers.projects

import tarot.application.commands.projects.ProjectCreateCommand
import tarot.application.handlers.CommandHandler
import tarot.domain.models.projects.ProjectId

trait ProjectCreateCommandHandler extends CommandHandler[ProjectCreateCommand, ProjectId]
