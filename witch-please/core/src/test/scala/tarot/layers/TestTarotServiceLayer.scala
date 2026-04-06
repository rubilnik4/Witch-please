package tarot.layers

import mocks.TelegramApiServiceMock
import shared.infrastructure.services.storage.*
import shared.infrastructure.services.telegram.*
import tarot.application.configurations.*
import tarot.infrastructure.repositories.TarotRepositoryLayer.Repositories
import tarot.infrastructure.services.authorize.*
import tarot.infrastructure.services.photo.*
import tarot.infrastructure.services.storage.TarotStorageLayer
import tarot.infrastructure.services.telegram.TelegramPublishServiceLive
import tarot.infrastructure.services.{TarotService, TarotServiceLive}
import zio.ZLayer

object TestTarotServiceLayer {
  private val telegramLayer: ZLayer[Any, Nothing, TelegramApiService] =
    TelegramApiServiceMock.live

  private val telegramPublishLayer =
    ZLayer.succeed(new TelegramPublishServiceLive())

  val live: ZLayer[TarotConfig & Repositories, Throwable, TarotService] =
    (
      ((telegramLayer ++ TarotStorageLayer.live) >>> PhotoServiceLayer.photoServiceLive) ++
        (TestTarotRepositoryLayer.live >>> AuthServiceLayer.live) ++
        TarotStorageLayer.live ++ ResourceFileServiceLayer.live ++ telegramLayer ++ telegramPublishLayer
    ) >>> ZLayer.fromFunction(TarotServiceLive.apply)
}
