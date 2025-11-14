package tarot.infrastructure.repositories.users

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

object UserRepositoryLayer {
  val live: ZLayer[Quill.Postgres[SnakeCase], Nothing, UserRepository] =
    ZLayer.fromFunction(quill => new UserRepositoryLive(quill))
}
