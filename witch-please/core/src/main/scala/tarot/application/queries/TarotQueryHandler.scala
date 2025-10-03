package tarot.application.queries

import tarot.application.queries.projects.ProjectsQueryHandler
import tarot.application.queries.users.UserByClientIdQueryHandler

trait TarotQueryHandler {
  def userByClientIdQueryHandler: UserByClientIdQueryHandler
  def projectsQueryHandler: ProjectsQueryHandler
}