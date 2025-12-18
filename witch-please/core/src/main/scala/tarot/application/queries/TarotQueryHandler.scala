package tarot.application.queries

import tarot.application.queries.cards.CardQueryHandler
import tarot.application.queries.cardsOfDay.CardOfDayQueryHandler
import tarot.application.queries.photos.PhotoQueryHandler
import tarot.application.queries.projects.ProjectQueryHandler
import tarot.application.queries.spreads.SpreadQueryHandler
import tarot.application.queries.users.UserQueryHandler

trait TarotQueryHandler {
  def userQueryHandler: UserQueryHandler
  def projectQueryHandler: ProjectQueryHandler
  def spreadQueryHandler: SpreadQueryHandler
  def cardQueryHandler: CardQueryHandler
  def cardOfDayQueryHandler: CardOfDayQueryHandler
  def photoQueryHandler: PhotoQueryHandler
}