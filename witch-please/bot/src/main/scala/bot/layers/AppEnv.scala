package bot.layers

import bot.application.configurations.AppConfig
import bot.application.handlers.BotCommandHandler
import bot.application.handlers.telegram.TelegramCommandHandler
import bot.infrastructure.repositories.BotRepository
import bot.infrastructure.services.*

trait AppEnv {
  def appConfig: AppConfig
  def botService: BotService
  def botRepository: BotRepository
  def botCommandHandler: BotCommandHandler
  //def tarotMeter: TarotMeter
  //def tarotTracing: TarotTracing
}
