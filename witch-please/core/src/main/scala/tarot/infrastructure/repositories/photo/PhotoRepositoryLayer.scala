package tarot.infrastructure.repositories.photo

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

object PhotoRepositoryLayer {
  val live: ZLayer[Quill.Postgres[SnakeCase], Nothing, PhotoRepository] =
    ZLayer.fromFunction(PhotoRepositoryLive(_))
}
