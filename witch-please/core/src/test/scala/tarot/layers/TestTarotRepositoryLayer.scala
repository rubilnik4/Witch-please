package tarot.layers

import com.dimafeng.testcontainers.PostgreSQLContainer
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import org.testcontainers.utility.DockerImageName
import tarot.application.configurations.TarotConfig
import tarot.infrastructure.repositories.TarotRepositoryLayer
import tarot.infrastructure.repositories.TarotRepositoryLayer.Repositories
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object TestTarotRepositoryLayer {
  private final val postgresVersion = DockerImageName.parse("postgres:17")

  private val postgresLayer: ZLayer[Any, Throwable, PostgreSQLContainer] =
    ZLayer.scoped {
      ZIO.acquireRelease(
        ZIO.attempt {
          val container = new PostgreSQLContainer(Some(postgresVersion))
          container.start()
          container
        }
      )(container => ZIO.attempt(container.stop()).orDie)
    }

  private val dataSourceLayer: ZLayer[PostgreSQLContainer, Throwable, DataSource] =
    ZLayer.fromZIO {
      for {
        container <- ZIO.service[PostgreSQLContainer]
        config = {
          val cfg = new HikariConfig()
          cfg.setJdbcUrl(container.jdbcUrl)
          cfg.setUsername(container.username)
          cfg.setPassword(container.password)
          cfg.setDriverClassName(container.driverClassName)
          cfg
        }
      } yield new HikariDataSource(config)
    }

  private val quillLayer: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase]] =
    ZLayer.fromZIO {
      ZIO.serviceWith[DataSource](dataSource =>
        new Quill.Postgres(SnakeCase, dataSource)
      )
    }

  private val repositoryLayerWithQuill: ZLayer[DataSource, Throwable, Repositories & Quill.Postgres[SnakeCase]] =
    TarotRepositoryLayer.repositoryLayer ++ quillLayer

  val live: ZLayer[TarotConfig, Throwable, Repositories] =
    postgresLayer >>> dataSourceLayer >>> TarotRepositoryLayer.repositoryLayer

  val liveWithQuill: ZLayer[TarotConfig, Throwable, Repositories & Quill.Postgres[SnakeCase]] =
    postgresLayer >>> dataSourceLayer >>> repositoryLayerWithQuill
}
