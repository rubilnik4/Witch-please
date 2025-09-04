package bot.infrastructure.repositories

import bot.application.configurations.BotConfig
import bot.infrastructure.repositories.sessions.*
import shared.infrastructure.services.*
import shared.infrastructure.services.telegram.*
import zio.ZLayer

object BotRepositoryLayer {    
  val botRepositoryLive: ZLayer[BotConfig, Throwable, BotRepository] =
    BotSessionRepositoryLayer.botSessionRepositoryLive 
      >>> ZLayer.fromFunction(BotRepositoryLive.apply)
}
