package tarot.infrastructure.repositories

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import tarot.application.configurations.AppConfig
import tarot.infrastructure.database.Migration
import tarot.infrastructure.repositories.auth.AuthRepositoryLayer
import tarot.infrastructure.repositories.spreads.SpreadRepositoryLayer
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object TarotRepositoryLayer {
  private val dataSourceLayer: ZLayer[AppConfig, Throwable, DataSource] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.service[AppConfig]

        postgresConfig <- ZIO.fromOption(config.postgres)
          .tapError(_ => ZIO.logError("Missing postgres config"))
          .orElseFail(new RuntimeException("Postgres config is missing"))

        hikariConfig = {
          val cfg = new HikariConfig()
          cfg.setJdbcUrl(postgresConfig.connectionString)
          cfg
        }
      } yield new HikariDataSource(hikariConfig)
    }

  val migrationLayer: ZLayer[DataSource, Nothing, Unit] =
    ZLayer.fromZIO {
      ZIO.serviceWithZIO[DataSource](dataSource =>
        Migration.applyMigrations(dataSource).orDie
      )
    }

  val quillLayer: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase]] =
    ZLayer.fromZIO {
      ZIO.serviceWith[DataSource](dataSource =>
        new Quill.Postgres(SnakeCase, dataSource)
      )
    }

  val tarotRepositoryLayer: ZLayer[DataSource, Nothing, TarotRepository] =
    quillLayer >>>
      (SpreadRepositoryLayer.spreadRepositoryLayer ++
       AuthRepositoryLayer.authRepositoryLayer) >>>
      ZLayer.fromFunction(TarotRepositoryLive.apply)
      
  val tarotRepositoryLive: ZLayer[AppConfig, Throwable, TarotRepository] =
    dataSourceLayer >>> ZLayer.scoped {
      for {
        dataSource <- ZIO.service[DataSource]
        _ <- Migration.applyMigrations(dataSource)        
        repository <- tarotRepositoryLayer.build.map(_.get[TarotRepository])
      } yield repository
    }
}
