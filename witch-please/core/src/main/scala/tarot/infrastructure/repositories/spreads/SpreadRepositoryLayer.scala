package tarot.infrastructure.repositories.spreads

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import tarot.application.configurations.TarotConfig
import tarot.infrastructure.database.Migration
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object SpreadRepositoryLayer {
  val spreadRepositoryLayer: ZLayer[Quill.Postgres[SnakeCase], Nothing, SpreadRepository] =
    ZLayer.fromFunction(quill => new SpreadRepositoryLive(quill))
}
