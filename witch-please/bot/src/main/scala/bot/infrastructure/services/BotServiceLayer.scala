package bot.infrastructure.services

import zio.ZLayer

object BotServiceLayer {
  val botServiceLive: ZLayer[AppConfig, Throwable, BotService] =
    (     
      TelegramApiServiceLayer.telegraApiServiceLive
      ) >>> ZLayer.fromFunction(BotServiceLive.apply)
}
