package tarot.infrastructure.repositories.cards

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

object CardRepositoryLayer {
  val live: ZLayer[Quill.Postgres[SnakeCase], Nothing, CardRepository] =
    ZLayer.fromFunction(CardRepositoryLive(_))
}
