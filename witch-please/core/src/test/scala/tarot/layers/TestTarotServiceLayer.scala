package tarot.layers

import mocks.TelegramApiServiceMock
import shared.infrastructure.services.storage.*
import shared.infrastructure.services.telegram.*
import tarot.application.configurations.*
import tarot.infrastructure.services.authorize.*
import tarot.infrastructure.services.photo.*
import tarot.infrastructure.services.storage.LocalFileStorageLayer
import tarot.infrastructure.services.{TarotService, TarotServiceLive}
import zio.ZLayer

object TestTarotServiceLayer {
  private val telegramLayer: ZLayer[Any, Nothing, TelegramApiService] =
    TelegramApiServiceMock.live

  private val storageLayer: ZLayer[TarotConfig, Throwable, FileStorageService] =
    LocalFileStorageLayer.storageLayer >>> FileStorageServiceLayer.localFileStorageServiceLive

  val live: ZLayer[TarotConfig, Throwable, TarotService] =
    (
      ((telegramLayer ++ storageLayer) >>> PhotoServiceLayer.photoServiceLive) ++
        (TestTarotRepositoryLayer.live >>> AuthServiceLayer.live) ++ 
        storageLayer ++ telegramLayer
    ) >>> ZLayer.fromFunction(TarotServiceLive.apply)
}
