package tarot.infrastructure.repositories.users

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import tarot.application.configurations.AppConfig
import tarot.infrastructure.database.Migration
import tarot.infrastructure.repositories.TarotRepository
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object UserRepositoryLayer {
  val userRepositoryLayer: ZLayer[Quill.Postgres[SnakeCase], Nothing, UserRepository] =
    ZLayer.fromFunction(quill => new UserRepositoryLive(quill))
}
