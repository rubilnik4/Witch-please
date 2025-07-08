package tarot.infrastructure.repositories.auth

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import tarot.application.configurations.AppConfig
import tarot.infrastructure.database.Migration
import tarot.infrastructure.repositories.TarotRepository
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object AuthRepositoryLayer {
  val authRepositoryLayer: ZLayer[Quill.Postgres[SnakeCase], Nothing, AuthRepository] =
    ZLayer.fromFunction(quill => new AuthRepositoryLive(quill))
}
