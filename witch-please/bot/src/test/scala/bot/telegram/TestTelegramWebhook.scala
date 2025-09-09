package bot.telegram

import bot.api.dto.*
import bot.application.commands.telegram.TelegramCommands

import java.time.Instant

object TestTelegramWebhook {
  private def getMessage(chatId: Long, text: Option[String], photo: Option[List[TelegramPhotoSizeRequest]]) =
    TelegramMessageRequest(
      messageId = 10L,
      from = Some(
        TelegramUserRequest(
          id = 12345L,
          isBot = false,
          firstName = "Test",
          lastName = None,
          username = Some("tester"),
          languageCode = Some("ru")
        )
      ),
      chat = TelegramChatRequest(
        id = chatId,
        `type` = "private",
        title = None,
        username = Some("tester"),
        firstName = Some("Test"),
        lastName = None
      ),
      date = Instant.now().getEpochSecond,
      text = text,
      photo = photo
    )

  private def textRequest(chatId: Long, text: String): TelegramWebhookRequest =
    TelegramWebhookRequest(
      updateId = 1L,
      message = Some(getMessage(chatId, Some(text), None)),
      editedMessage = None,
      callbackQuery = None,
      inlineQuery = None
    )

  def photoRequest(chatId: Long, fileId: String): TelegramWebhookRequest = {
    val photo = List(
      TelegramPhotoSizeRequest(
        fileId = fileId,
        fileUniqueId = java.util.UUID.randomUUID().toString.take(10),
        width = 800,
        height = 600,
        fileSize = Some(12345)
      )
    )
    TelegramWebhookRequest(
      updateId = 2L,
      message = Some(getMessage(chatId, None, Some(photo))),
      editedMessage = None,
      callbackQuery = None,
      inlineQuery = None
    )
  }

  def startRequest(chatId: Long): TelegramWebhookRequest =
    textRequest(chatId, TelegramCommands.Start)

  def createProjectRequest(chatId: Long): TelegramWebhookRequest =
    textRequest(chatId, s"${TelegramCommands.ProjectCreate} Test project")

  def createSpreadRequest(chatId: Long, cardCount: Int): TelegramWebhookRequest =
    textRequest(chatId, s"${TelegramCommands.SpreadCreate} $cardCount Test spread")

  def createCardRequest(chatId: Long, cardIndex: Int): TelegramWebhookRequest =
    textRequest(chatId, s"${TelegramCommands.CardCreate} $cardIndex Test card $cardIndex")

  def publishSpreadRequest(chatId: Long, scheduledAt: Instant): TelegramWebhookRequest =
    textRequest(chatId, s"${TelegramCommands.SpreadPublish} ${scheduledAt.getEpochSecond}")
}
