package tarot.application.queries

import tarot.application.queries.cards.CardsQueryHandler
import tarot.application.queries.projects.ProjectsQueryHandler
import tarot.application.queries.spreads.SpreadsQueryHandler
import tarot.application.queries.users.UserQueryHandler

trait TarotQueryHandler {
  def userQueryHandler: UserQueryHandler
  def projectsQueryHandler: ProjectsQueryHandler
  def spreadsQueryHandler: SpreadsQueryHandler
  def cardsQueryHandler: CardsQueryHandler
}