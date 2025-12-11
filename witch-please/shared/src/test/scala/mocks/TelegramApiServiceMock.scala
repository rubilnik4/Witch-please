package mocks

import shared.api.dto.telegram.*
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.api.ApiError
import shared.models.telegram.TelegramFile
import zio.{ULayer, ZIO, ZLayer}

final class TelegramApiServiceMock extends TelegramApiService {
  override def sendPhoto(chatId: Long, fileId: String): ZIO[Any, ApiError, String] =
    ZIO.succeed(s"mock-photo-$fileId")

  override def sendPhoto(chatId: Long, photo: TelegramFile): ZIO[Any, ApiError, String] =
    ZIO.succeed(s"mock-upload-${photo.fileName}")

  override def sendText(chatId: Long, text: String): ZIO[Any, ApiError, Long] =
    ZIO.succeed(1L)

  override def sendReplyText(chatId: Long, text: String): ZIO[Any, ApiError, Long] =
    ZIO.succeed(1L)

  override def sendButton(chatId: Long, text: String, button: TelegramKeyboardButton): ZIO[Any, ApiError, Long] =
    ZIO.succeed(1L)

  override def sendButtons(chatId: Long, text: String, buttons: List[TelegramKeyboardButton]): ZIO[Any, ApiError, Long] =
    ZIO.succeed(1L)

  override def sendInlineButton(chatId: Long, text: String, button: TelegramInlineKeyboardButton): ZIO[Any, ApiError, Long] =
    ZIO.succeed(1L)

  override def sendInlineButtons(chatId: Long, text: String, buttons: List[TelegramInlineKeyboardButton]): ZIO[Any, ApiError, Long] =
    ZIO.succeed(1L)

  override def sendInlineGroupButtons(chatId: Long, text: String, buttons: List[List[TelegramInlineKeyboardButton]]): ZIO[Any, ApiError, Long] =
    ZIO.succeed(1L)

  override def downloadPhoto(fileId: String): ZIO[Any, ApiError, TelegramFile] =
    ZIO.succeed(TelegramFile(fileId, Array.emptyByteArray))
}

object TelegramApiServiceMock {
  val live: ULayer[TelegramApiServiceMock] =
    ZLayer.succeed(new TelegramApiServiceMock())
}