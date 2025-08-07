package bot.layers

import bot.application.configurations.AppConfig
import zio.ZLayer

object AppEnvLayer {
  val appEnvLive: ZLayer[
    AppConfig,
//      & TarotService
//      & TarotRepository
//      & TarotCommandHandler
//      & TarotMeter & TarotTracing,
    Nothing,
    AppEnv
  ] =
    ZLayer.fromFunction(AppEnvLive.apply)
}
