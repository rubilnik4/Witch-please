package bot.layers

import bot.application.configurations.AppConfig

trait AppEnv {
  def appConfig: AppConfig
  //def tarotMeter: TarotMeter
  //def tarotTracing: TarotTracing
}
