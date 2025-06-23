package tarot.layers

import tarot.application.configurations.{AppConfig, CacheConfig, LocalStorageConfig, ProjectConfig, TelegramConfig}
import zio.ZLayer
import zio.durationInt

object TestAppConfigLayer {
  private lazy val appConfig: AppConfig =
    AppConfig(
      project = ProjectConfig(),
      postgres = None,
      telemetry = None,
      cache = CacheConfig(),
      telegram = TelegramConfig(
        token = ""
      ),
      localStorage = Some(LocalStorageConfig(
        path = ""
      )),
    )

  val testAppConfigLive: ZLayer[Any, Nothing, AppConfig] =
    ZLayer.succeed(appConfig)
}
