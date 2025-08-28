package bot.layers

import bot.application.configurations.AppConfig
import bot.application.handlers.*
import bot.application.handlers.telegram.TelegramCommandHandler
import bot.infrastructure.repositories.*
import bot.infrastructure.services.*

final case class AppEnvLive (
  appConfig: AppConfig,
  botService: BotService,
  botRepository: BotRepository,
  botCommandHandler: BotCommandHandler,
//  tarotMeter: TarotMeter,
//  tarotTracing: TarotTracing
) extends AppEnv
