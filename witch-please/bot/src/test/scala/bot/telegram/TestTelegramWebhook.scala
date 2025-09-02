package bot.telegram

import bot.api.dto.*

import java.time.Instant

object TestTelegramWebhook {
  val startJson: String =
    s"""
       |{
       |  "update_id": 1,
       |  "message": {
       |    "message_id": 10,
       |    "from": {
       |      "id": 12345,
       |      "is_bot": false,
       |      "first_name": "Test",
       |      "username": "tester"
       |    },
       |    "chat": {
       |      "id": 12345,
       |      "type": "private"
       |    },
       |    "date": ${Instant.now.getEpochSecond},
       |    "text": "/start"
       |  }
       |}
       |""".stripMargin

  def getStartRequest(chatId: Long): TelegramWebhookRequest =
    TelegramWebhookRequest(
      updateId = 1L,
      message = Some(
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
          text = Some("/start"),
          photo = None
        )
      ),
      editedMessage = None,
      callbackQuery = None,
      inlineQuery = None
    )
}
