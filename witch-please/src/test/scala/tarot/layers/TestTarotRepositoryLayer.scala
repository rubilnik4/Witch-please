package tarot.layers

import com.dimafeng.testcontainers.PostgreSQLContainer
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.testcontainers.utility.DockerImageName
import tarot.infrastructure.database.Migration
import tarot.infrastructure.repositories.{TarotRepository, TarotRepositoryLayer}
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

  val postgresTarotRepositoryLive: ZLayer[Any, Throwable, TarotRepository] =
    postgresLayer >>> dataSourceLayer >>> ZLayer.scoped {
      for {
        dataSource <- ZIO.service[DataSource]
        _ <- Migration.applyMigrations(dataSource)
        layer =
          TarotRepositoryLayer.quillLayer >>>
            TarotRepositoryLayer.tarotRepositoryLayer
        repository <- layer.build.map(_.get[TarotRepository])
      } yield repository
    }
}
