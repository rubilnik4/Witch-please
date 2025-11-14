package tarot.infrastructure.repositories.projects

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

object ProjectRepositoryLayer {
  val live: ZLayer[Quill.Postgres[SnakeCase], Nothing, ProjectRepository] =
    ZLayer.fromFunction(quill => new ProjectRepositoryLive(quill))
}

