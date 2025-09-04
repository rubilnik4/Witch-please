package bot.layers

import bot.application.configurations.BotConfig
import bot.application.handlers.*
import bot.application.handlers.telegram.TelegramCommandHandler
import bot.infrastructure.repositories.*
import bot.infrastructure.services.*

final case class BotEnvLive(
  appConfig: BotConfig,
  botService: BotService,
  botRepository: BotRepository,
  botCommandHandler: BotCommandHandler,
  //  tarotMeter: TarotMeter,
  //  tarotTracing: TarotTracing
) extends BotEnv
