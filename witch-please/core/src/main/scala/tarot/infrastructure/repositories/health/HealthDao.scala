package tarot.infrastructure.repositories.health

import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.ZIO

import java.sql.SQLException

final class HealthDao(quill: Quill.Postgres[SnakeCase]) {
  import quill.*

  def ready: ZIO[Any, SQLException, Unit] =
    run(quote {
      sql"SELECT 1".as[Query[Int]]
    }).unit
}
