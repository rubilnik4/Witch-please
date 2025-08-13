package bot.layers

import bot.application.configurations.AppConfig
import bot.application.handlers.TelegramCommandHandler
import bot.infrastructure.repositories.BotRepository
import bot.infrastructure.services.*

trait AppEnv {
  def appConfig: AppConfig
  def botService: BotService
  def botRepository: BotRepository
  def telegramCommandService: TelegramCommandHandler
  //def tarotMeter: TarotMeter
  //def tarotTracing: TarotTracing
}
