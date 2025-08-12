package bot.layers

import bot.application.configurations.AppConfig
import bot.infrastructure.services.*

trait AppEnv {
  def appConfig: AppConfig,
  def botService: BotService
  //def tarotMeter: TarotMeter
  //def tarotTracing: TarotTracing
}
