package tarot.application.queries

import tarot.application.queries.cards.*
import tarot.application.queries.cardsOfDay.CardOfDayQueryHandlerLayer
import tarot.application.queries.channels.UserChannelQueryHandlerLayer
import tarot.application.queries.photos.PhotoQueryHandlerLayer
import tarot.application.queries.projects.*
import tarot.application.queries.spreads.*
import tarot.application.queries.users.*
import tarot.infrastructure.repositories.TarotRepositoryLayer
import tarot.infrastructure.repositories.TarotRepositoryLayer.Repositories
import zio.ZLayer

object TarotQueryHandlerLayer {
  val live: ZLayer[Repositories, Throwable, TarotQueryHandler] =
    (
      UserQueryHandlerLayer.live ++
      UserChannelQueryHandlerLayer.live ++
      ProjectQueryHandlerLayer.live ++
      SpreadQueryHandlerLayer.live ++
      CardQueryHandlerLayer.live ++
      CardOfDayQueryHandlerLayer.live ++
      PhotoQueryHandlerLayer.live
    ) >>> ZLayer.fromFunction(TarotQueryHandlerLive.apply)
}
