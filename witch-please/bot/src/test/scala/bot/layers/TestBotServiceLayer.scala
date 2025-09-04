package bot.layers

import bot.application.configurations.BotConfig
import bot.infrastructure.services.*
import bot.infrastructure.services.sessions.{BotSessionService, BotSessionServiceLayer}
import bot.infrastructure.services.tarot.TarotApiService
import bot.mocks.*
import shared.infrastructure.services.*
import shared.infrastructure.services.telegram.{TelegramApiService, TelegramApiServiceLayer}
import zio.ZLayer

object TestBotServiceLayer {
  val botServiceLive: ZLayer[BotConfig, Throwable, BotService] =
    (
      BotServiceLayer.telegramLayer ++ BotServiceLayer.storageLayer ++
        TarotApiServiceMock.tarotApiServiceLive ++
        BotSessionServiceLayer.botSessionServiceLive
      ) >>> ZLayer.fromFunction(BotServiceLive.apply)
}
