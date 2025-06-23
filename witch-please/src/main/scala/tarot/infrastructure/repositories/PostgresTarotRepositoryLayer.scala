package tarot.infrastructure.repositories

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import tarot.application.configurations.AppConfig
import tarot.infrastructure.database.Migration
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object PostgresTarotRepositoryLayer {
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

  val postgresTarotRepositoryLayer : ZLayer[Quill.Postgres[SnakeCase], Nothing, TarotRepository] =
    ZLayer.fromFunction(quill => new PostgresTarotRepositoryLive(quill))

  val postgresMarketRepositoryLive : ZLayer[AppConfig, Throwable, TarotRepository] =
    dataSourceLayer >>> ZLayer.scoped {
      for {
        dataSource <- ZIO.service[DataSource]
        _ <- Migration.applyMigrations(dataSource)
        layer =
          PostgresTarotRepositoryLayer.quillLayer >>> PostgresTarotRepositoryLayer.postgresTarotRepositoryLayer
        repository <- layer.build.map(_.get[TarotRepository])
      } yield repository
  }
}
