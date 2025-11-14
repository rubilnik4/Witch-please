package tarot.infrastructure.repositories.projects

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import tarot.application.configurations.TarotConfig
import tarot.infrastructure.database.Migration
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object ProjectRepositoryLayer {
  val projectRepositoryLayer: ZLayer[Quill.Postgres[SnakeCase], Nothing, ProjectRepository] =
    ZLayer.fromFunction(quill => new ProjectRepositoryLive(quill))
}
