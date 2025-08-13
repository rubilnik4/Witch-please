package bot.layers

import bot.application.configurations.AppConfig
import bot.application.handlers.TelegramCommandHandler
import bot.infrastructure.services.*

trait AppEnv {
  def appConfig: AppConfig
  def botService: BotService
  def telegramCommandService: TelegramCommandHandler
  //def tarotMeter: TarotMeter
  //def tarotTracing: TarotTracing
}
