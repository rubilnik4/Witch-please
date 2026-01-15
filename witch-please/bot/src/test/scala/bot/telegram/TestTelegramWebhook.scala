package bot.telegram

import bot.api.dto.*
import bot.application.commands.telegram.*

import java.time.*
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

  def startAuthorRequest(chatId: Long): TelegramWebhookRequest =
    textRequest(chatId, AuthorCommands.Start)
    
  def createSpreadRequest(chatId: Long): TelegramWebhookRequest =
    textRequest(chatId, s"${AuthorCommands.SpreadCreate}")

  def selectSpreadRequest(chatId: Long, spreadId: UUID): TelegramWebhookRequest =
    textRequest(chatId, AuthorCommands.spreadSelect(spreadId))

  def updateSpreadRequest(chatId: Long, spreadId: UUID): TelegramWebhookRequest =
    textRequest(chatId, AuthorCommands.spreadEdit(spreadId))

  def deleteSpreadRequest(chatId: Long, spreadId: UUID): TelegramWebhookRequest =
    textRequest(chatId, AuthorCommands.spreadDelete(spreadId))  
    
  def createCardRequest(chatId: Long, position: Int): TelegramWebhookRequest =
    textRequest(chatId, AuthorCommands.cardCreate(position))

  def updateCardRequest(chatId: Long, cardId: UUID): TelegramWebhookRequest =
    textRequest(chatId, AuthorCommands.cardEdit(cardId))
    
  def deleteCardRequest(chatId: Long, cardId: UUID): TelegramWebhookRequest =
    textRequest(chatId, AuthorCommands.cardDelete(cardId))

  def createCardOfDayRequest(chatId: Long): TelegramWebhookRequest =
    textRequest(chatId, AuthorCommands.CardOfDayCreate)

  def updateCardOfDayRequest(chatId: Long, cardOfDayId: UUID): TelegramWebhookRequest =
    textRequest(chatId, AuthorCommands.cardOfDayEdit(cardOfDayId))

  def deleteCardOfDayRequest(chatId: Long, cardOfDayId: UUID): TelegramWebhookRequest =
    textRequest(chatId, AuthorCommands.cardOfDayDelete(cardOfDayId))
    
  def publishSpreadRequest(chatId: Long, spreadId: UUID): TelegramWebhookRequest =
    textRequest(chatId, AuthorCommands.spreadPublish(spreadId))
    
  def scheduleSelectMonth(chatId: Long, month: YearMonth): TelegramWebhookRequest =
    textRequest(chatId, SchedulerCommands.selectMonth(month))

  def scheduleSelectDate(chatId: Long, date: LocalDate): TelegramWebhookRequest =
    textRequest(chatId, SchedulerCommands.selectDate(date))

  def scheduleSelectTime(chatId: Long, time: LocalTime): TelegramWebhookRequest =
    textRequest(chatId, SchedulerCommands.selectTime(time))

  def scheduleSelectCardOfDayDelay(chatId: Long, delay: Duration): TelegramWebhookRequest =
    textRequest(chatId, SchedulerCommands.selectCardOfDayDelay(delay))

  def scheduleConfirm(chatId: Long): TelegramWebhookRequest =
    textRequest(chatId, SchedulerCommands.Confirm)
}
