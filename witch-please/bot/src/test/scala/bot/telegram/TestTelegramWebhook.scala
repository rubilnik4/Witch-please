package bot.telegram

import bot.api.dto.*
import bot.application.commands.telegram.TelegramCommands

import java.time.Instant
import java.util.UUID

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

  def textRequest(chatId: Long, text: String): TelegramWebhookRequest =
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
    textRequest(chatId, s"${TelegramCommands.ProjectCreate}")

  def selectProjectRequest(chatId: Long, projectId: UUID): TelegramWebhookRequest =
    textRequest(chatId, s"${TelegramCommands.projectSelectCommand(projectId)}")
    
  def createSpreadRequest(chatId: Long): TelegramWebhookRequest =
    textRequest(chatId, s"${TelegramCommands.SpreadCreate}")

  def selectSpreadsRequest(chatId: Long, spreadId: UUID, cardCount: Int): TelegramWebhookRequest =
    textRequest(chatId, TelegramCommands.spreadSelectCommand(spreadId, cardCount))

  def createCardRequest(chatId: Long, index: Int): TelegramWebhookRequest =
    textRequest(chatId, TelegramCommands.cardCreateCommand(index))

  def selectCardsRequest(chatId: Long, cardId: UUID, index: Int): TelegramWebhookRequest =
    textRequest(chatId, TelegramCommands.cardSelectCommand(cardId, index))
    
  def publishSpreadRequest(chatId: Long, scheduledAt: Instant): TelegramWebhookRequest =
    textRequest(chatId, s"${TelegramCommands.SpreadPublish} ${scheduledAt.getEpochSecond}")
}
