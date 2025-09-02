package bot.domain.models.telegram

import bot.api.dto.*

sealed trait TelegramMessage()

object TelegramMessage {
  final case class Text(context: TelegramContext, text: String) extends TelegramMessage
  final case class Command(context: TelegramContext, command: String) extends TelegramMessage
  final case class Photo(context: TelegramContext, fileId: String) extends TelegramMessage
  final case class Unknown() extends TelegramMessage

  def fromRequest(request: TelegramWebhookRequest): TelegramMessage =
    request.message.map(parseMessage)
      .orElse(request.callbackQuery.map(parseCallback))
      .getOrElse(Unknown())

  private def parseMessage(message: TelegramMessageRequest): TelegramMessage = {
    val context = getContext(message)
    message.text match {
      case Some(text) if text.startsWith("/") =>
        TelegramMessage.Command(context, text.drop(1))
      case Some(text) =>
        TelegramMessage.Text(context, text)
      case None =>
        message.photo.flatMap(_.lastOption.map(_.fileId)) match {
          case Some(fileId) => TelegramMessage.Photo(context, fileId)
          case None => Unknown()
        }
    }
  }

  private def parseCallback(callback: TelegramCallbackQueryRequest): TelegramMessage = {
    val context = getContext(callback)
    callback.data match {
      case Some(data) => Command(context, data)    
      case _ => Unknown()
    }
  }

  private def getContext(message: TelegramMessageRequest): TelegramContext =
    TelegramContext(
      chatId = message.chat.id,
      clientId = message.from.map(_.id).getOrElse(message.chat.id),
      firstName = message.from.flatMap(user => Option(user.firstName)),
      username = message.from.flatMap(_.username)
    )


  private def getContext(callback: TelegramCallbackQueryRequest): TelegramContext =
    TelegramContext(
      chatId = callback.message.map(_.chat.id).getOrElse(callback.from.id),
      clientId = callback.from.id,
      firstName = Some(callback.from.firstName),
      username = callback.from.username
    )
}
