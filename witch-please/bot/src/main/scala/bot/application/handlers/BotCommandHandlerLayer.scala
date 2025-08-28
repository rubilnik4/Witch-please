package bot.application.handlers

import bot.application.handlers.telegram.*
import zio.{ULayer, ZLayer}

object BotCommandHandlerLayer {
  val botCommandHandlerLive: ULayer[BotCommandHandlerLive] =
    ZLayer.succeed(new TelegramCommandHandlerLive) >>> ZLayer.fromFunction(BotCommandHandlerLive.apply)
}
