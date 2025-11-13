package tarot.layers

import shared.infrastructure.services.storage.*
import shared.infrastructure.services.telegram.*
import tarot.application.configurations.*
import tarot.infrastructure.services.authorize.*
import tarot.infrastructure.services.photo.*
import tarot.infrastructure.services.storage.LocalFileStorageLayer
import tarot.infrastructure.services.{TarotService, TarotServiceLive}
import tarot.mocks.TelegramApiServiceMock
import zio.ZLayer

object TestTarotServiceLayer {
  private val telegramLayer: ZLayer[TarotConfig, Throwable, TelegramApiService] =
    TelegramApiServiceMock.telegramApiServiceLive

  private val storageLayer: ZLayer[TarotConfig, Throwable, FileStorageService] =
    LocalFileStorageLayer.storageLayer >>> FileStorageServiceLayer.localFileStorageServiceLive

  val tarotServiceLive: ZLayer[TarotConfig, Throwable, TarotService] =
    (
      ((telegramLayer ++ storageLayer) >>> PhotoServiceLayer.photoServiceLive) ++
      AuthServiceLayer.authServiceLive ++ storageLayer ++ telegramLayer
    ) >>> ZLayer.fromFunction(TarotServiceLive.apply)
}
