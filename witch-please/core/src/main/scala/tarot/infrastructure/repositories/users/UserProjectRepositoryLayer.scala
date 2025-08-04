package tarot.infrastructure.repositories.users

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import tarot.application.configurations.AppConfig
import tarot.infrastructure.database.Migration
import tarot.infrastructure.repositories.TarotRepository
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object UserProjectRepositoryLayer {
  val userProjectRepositoryLayer: ZLayer[Quill.Postgres[SnakeCase], Nothing, UserProjectRepository] =
    ZLayer.fromFunction(quill => new UserProjectRepositoryLive(quill))
}
