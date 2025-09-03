package tarot.application.commands.projects

import tarot.application.commands.CommandHandler
import tarot.application.commands.projects.ProjectCreateCommand
import tarot.domain.models.projects.ProjectId

trait ProjectCreateCommandHandler extends CommandHandler[ProjectCreateCommand, ProjectId]
