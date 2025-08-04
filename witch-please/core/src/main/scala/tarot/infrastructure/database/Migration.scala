package tarot.infrastructure.database

import org.flywaydb.core.Flyway
import zio.*

import javax.sql.DataSource

object Migration {
  def applyMigrations(dataSource: DataSource): Task[Unit] =
    ZIO.attempt {
      val flyway = Flyway
        .configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
      flyway.migrate()
      ()
    }
}
