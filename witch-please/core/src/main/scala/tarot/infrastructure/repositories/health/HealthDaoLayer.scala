package tarot.infrastructure.repositories.health

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

object HealthDaoLayer {
  val live: ZLayer[Quill.Postgres[SnakeCase], Nothing, HealthDao] =
    ZLayer.fromFunction(HealthDao.apply)
}
