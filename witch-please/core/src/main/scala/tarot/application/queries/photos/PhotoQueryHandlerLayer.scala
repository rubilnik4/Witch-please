package tarot.application.queries.photos

import tarot.infrastructure.repositories.cards.CardRepository
import tarot.infrastructure.repositories.photo.PhotoRepository
import zio.ZLayer

object PhotoQueryHandlerLayer {
  val live: ZLayer[PhotoRepository, Nothing, PhotoQueryHandler] =
    ZLayer.fromFunction(new PhotoQueryHandlerLive(_))
}
