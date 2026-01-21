package tarot.infrastructure.repositories.channels

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

object UserChannelRepositoryLayer {
  val live: ZLayer[Quill.Postgres[SnakeCase], Nothing, UserChannelRepository] =
    ZLayer.fromFunction(quill => new UserChannelRepositoryLive(quill))
}
