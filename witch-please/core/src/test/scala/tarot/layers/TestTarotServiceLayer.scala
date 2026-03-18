package tarot.layers

import mocks.TelegramApiServiceMock
import shared.infrastructure.services.storage.*
import shared.infrastructure.services.telegram.*
import tarot.application.configurations.*
import tarot.infrastructure.services.authorize.*
import tarot.infrastructure.services.photo.*
import tarot.infrastructure.services.storage.TarotStorageLayer
import tarot.infrastructure.services.{TarotService, TarotServiceLive}
import zio.ZLayer

object TestTarotServiceLayer {
  private val telegramLayer: ZLayer[Any, Nothing, TelegramApiService] =
    TelegramApiServiceMock.live

  val live: ZLayer[TarotConfig, Throwable, TarotService] =
    (
      ((telegramLayer ++ TarotStorageLayer.live) >>> PhotoServiceLayer.photoServiceLive) ++
        (TestTarotRepositoryLayer.live >>> AuthServiceLayer.live) ++
        TarotStorageLayer.live ++ ResourceFileServiceLayer.live ++telegramLayer
    ) >>> ZLayer.fromFunction(TarotServiceLive.apply)
}
