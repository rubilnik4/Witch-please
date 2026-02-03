package mocks

import shared.api.dto.telegram.*
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.api.ApiError
import shared.models.telegram.TelegramFile
import zio.{ULayer, ZIO, ZLayer}

final class TelegramApiServiceMock extends TelegramApiService {
  override def getBot: ZIO[Any, ApiError, TelegramBotResponse] =
    ZIO.succeed(
      TelegramBotResponse(
        id = 12345,
        isBot = true,
        firstName = "Test Bot",
        username = Some("test_tarot_channel")
      )
    )

  override def getChat(chatId: Long): ZIO[Any, ApiError, TelegramChatResponse] =
    if (chatId == 0L) ZIO.fail(ApiError.HttpCode(400, "chat_id must be non-zero"))
    else
      ZIO.succeed(
        TelegramChatResponse(
          id = chatId,
          `type` = "channel",
          title = Some("Test Tarot Channel"),
          username = Some("test_tarot_channel")
        )
      )

  override def getChatMember(chatId: Long, userId: Long): ZIO[Any, ApiError, TelegramChatMemberResponse] =
    if (chatId == 0L) ZIO.fail(ApiError.HttpCode(400, "chat_id must be non-zero"))
    else if (userId == 0L) ZIO.fail(ApiError.HttpCode(400, "user_id must be non-zero"))
    else
      ZIO.succeed(
        TelegramChatMemberResponse(
          status = TelegramChatMemberStatus.Administrator,
          canPostMessages = Some(true)
        )
      )

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

  override def sendPhotos(chatId: Long, text: String, fileIds: List[String]): ZIO[Any, ApiError, Long] =
    ZIO.succeed(1L)   

  override def downloadPhoto(fileId: String): ZIO[Any, ApiError, TelegramFile] =
    ZIO.succeed(TelegramFile(fileId, Array.emptyByteArray))
}

object TelegramApiServiceMock {
  val live: ULayer[TelegramApiServiceMock] =
    ZLayer.succeed(new TelegramApiServiceMock())
}