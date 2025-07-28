package tarot.application.commands.projects

import tarot.domain.models.authorize.UserId
import tarot.domain.models.projects.ExternalProject


case class ProjectCreateCommand(externalProject: ExternalProject, userId: UserId)
