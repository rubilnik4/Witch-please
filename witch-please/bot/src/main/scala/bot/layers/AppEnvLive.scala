package bot.layers

import bot.application.configurations.AppConfig

final case class AppEnvLive (
  appConfig: AppConfig,
//  tarotService: TarotService,
//  tarotRepository: TarotRepository,
//  tarotCommandHandler: TarotCommandHandler,
//  tarotMeter: TarotMeter,
//  tarotTracing: TarotTracing
) extends AppEnv
