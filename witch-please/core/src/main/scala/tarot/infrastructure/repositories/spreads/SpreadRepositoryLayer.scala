package tarot.infrastructure.repositories.spreads

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

object SpreadRepositoryLayer {
  val live: ZLayer[Quill.Postgres[SnakeCase], Nothing, SpreadRepository] =
    ZLayer.fromFunction(quill => new SpreadRepositoryLive(quill))
}
