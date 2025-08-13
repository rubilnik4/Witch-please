package bot.infrastructure.repositories

import bot.application.configurations.AppConfig
import bot.infrastructure.repositories.sessions.*
import shared.infrastructure.services.*
import shared.infrastructure.services.telegram.*
import zio.ZLayer

object BotRepositoryLayer {    
  val botRepositoryLive: ZLayer[AppConfig, Throwable, BotRepository] =
    BotSessionRepositoryLayer.botSessionRepositoryLive 
      >>> ZLayer.fromFunction(BotRepositoryLive.apply)
}
