package tarot.infrastructure.repositories

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import tarot.application.configurations.TarotConfig
import tarot.infrastructure.database.Migration
import tarot.infrastructure.repositories.projects.*
import tarot.infrastructure.repositories.spreads.*
import tarot.infrastructure.repositories.users.*
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object TarotRepositoryLayer {
  type Repositories =
    UserRepository & UserProjectRepository & ProjectRepository & SpreadRepository

  private val dataSourceLayer: ZLayer[TarotConfig, Throwable, DataSource] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.service[TarotConfig]

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

  private val quillLayer: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase]] =
    ZLayer.fromZIO {
      ZIO.serviceWith[DataSource](dataSource =>
        new Quill.Postgres(SnakeCase, dataSource)
      )
    }

  private val migrationLayer: ZLayer[DataSource, Throwable, Unit] =
    ZLayer.scoped {
      for {
        dataSource <- ZIO.service[DataSource]
        _ <- Migration.applyMigrations(dataSource)
      } yield ()
    }
    
  val repositoryLayer: ZLayer[DataSource, Throwable, Repositories] =
    migrationLayer ++ quillLayer >>>
      SpreadRepositoryLayer.spreadRepositoryLayer ++
      ProjectRepositoryLayer.projectRepositoryLayer ++
      UserRepositoryLayer.userRepositoryLayer ++
      UserProjectRepositoryLayer.userProjectRepositoryLayer
    
  val live: ZLayer[TarotConfig, Throwable, Repositories] =
    dataSourceLayer >>> repositoryLayer
}
