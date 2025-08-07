package bot.domain.models

import bot.api.dto.*

sealed trait TelegramMessage

object TelegramMessage {
  final case class Text(chatId: Long, text: String) extends TelegramMessage
  final case class Command(chatId: Long, command: String) extends TelegramMessage
  final case class Photo(chatId: Long, fileId: String) extends TelegramMessage
  final case class Unknown() extends TelegramMessage

  def fromRequest(request: TelegramWebhookRequest): TelegramMessage =
    request.message.map(parseMessage)
      .orElse(request.callbackQuery.map(parseCallback))
      .getOrElse(Unknown())

  private def parseMessage(message: TelegramMessageRequest): TelegramMessage =
    message.text match {
      case Some(text) if text.startsWith("/") =>
        Command(message.chat.id, text.drop(1))
      case Some(text) =>
        Text(message.chat.id, text)
      case None =>
        message.photo.flatMap(_.lastOption.map(_.fileId)) match {
          case Some(fileId) => Photo(message.chat.id, fileId)
          case None => Unknown()
        }
    }

  private def parseCallback(callback: TelegramCallbackQueryRequest): TelegramMessage =
    (callback.message.map(_.chat.id), callback.data) match {
      case (Some(chatId), Some(data)) => Command(chatId, data)
      case (None, Some(data)) => Command(callback.from.id, data)
      case _ => Unknown()
    }
}
