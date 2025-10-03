package bot.layers

import bot.application.configurations.BotConfig
import bot.infrastructure.services.*
import bot.infrastructure.services.BotServiceLayer.telegramConfigLayer
import bot.infrastructure.services.sessions.{BotSessionService, BotSessionServiceLayer}
import bot.infrastructure.services.tarot.TarotApiService
import bot.mocks.*
import shared.infrastructure.services.*
import shared.infrastructure.services.telegram.{TelegramApiService, TelegramApiServiceLayer, TelegramWebhookLayer}
import zio.ZLayer

object TestBotServiceLayer {
  val botServiceLive: ZLayer[BotConfig, Throwable, BotService] =
    (
      (BotServiceLayer.telegramTokenLayer >>> TelegramApiServiceLayer.telegramChannelServiceLive) ++
      (BotServiceLayer.telegramConfigLayer >>> TelegramWebhookLayer.telegramWebhookLive) ++
      BotServiceLayer.storageLayer ++
      TarotApiServiceMock.tarotApiServiceLive ++
      BotSessionServiceLayer.botSessionServiceLive
      ) >>> ZLayer.fromFunction(BotServiceLive.apply)
}
