package tarot.application.commands.photos

import tarot.application.queries.cards.CardQueryHandler
import tarot.infrastructure.repositories.cards.CardRepository
import tarot.infrastructure.repositories.photo.PhotoRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}
import zio.ZLayer

object PhotoCommandHandlerLayer {
  val live: ZLayer[PhotoRepository, Nothing, PhotoCommandHandler] =
    ZLayer.fromFunction(new PhotoCommandHandlerLive(_))
}
