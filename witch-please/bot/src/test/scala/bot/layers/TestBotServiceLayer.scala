package bot.layers

import bot.application.configurations.BotConfig
import bot.infrastructure.repositories.BotRepositoryLayer
import bot.infrastructure.services.*
import bot.infrastructure.services.BotServiceLayer.telegramConfigLayer
import bot.infrastructure.services.sessions.{BotSessionService, BotSessionServiceLayer}
import bot.infrastructure.services.tarot.{TarotApiService, TarotApiServiceLayer, TarotApiUrl}
import mocks.TelegramApiServiceMock
import shared.infrastructure.services.*
import shared.infrastructure.services.telegram.{TelegramApiService, TelegramWebhookLayer}
import zio.ZLayer

object TestBotServiceLayer {
  val live: ZLayer[BotConfig & TarotApiUrl, Throwable, BotService] =
    (
      TelegramApiServiceMock.live ++
      (BotServiceLayer.telegramConfigLayer >>> TelegramWebhookLayer.telegramWebhookLive) ++
      BotServiceLayer.storageLayer ++
      TarotApiServiceLayer.live ++
      (BotRepositoryLayer.live >>> BotSessionServiceLayer.live)
    ) >>> ZLayer.fromFunction(BotServiceLive.apply)
}
