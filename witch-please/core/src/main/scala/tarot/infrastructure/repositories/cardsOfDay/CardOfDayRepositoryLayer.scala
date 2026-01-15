package tarot.infrastructure.repositories.cardsOfDay

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

object CardOfDayRepositoryLayer {
  val live: ZLayer[Quill.Postgres[SnakeCase], Nothing, CardOfDayRepository] =
    ZLayer.fromFunction(CardOfDayRepositoryLive(_))
}
