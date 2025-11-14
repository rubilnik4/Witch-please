package tarot.infrastructure.repositories.users

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

object UserProjectRepositoryLayer {
  val live: ZLayer[Quill.Postgres[SnakeCase], Nothing, UserProjectRepository] =
    ZLayer.fromFunction(quill => new UserProjectRepositoryLive(quill))
}
