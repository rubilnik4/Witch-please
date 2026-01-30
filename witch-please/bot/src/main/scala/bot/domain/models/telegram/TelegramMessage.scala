package bot.domain.models.telegram

import bot.api.dto.*

sealed trait TelegramMessage()

object TelegramMessage {
  final case class Text(context: TelegramContext, text: String) extends TelegramMessage
  final case class Command(context: TelegramContext, command: String) extends TelegramMessage
  final case class Forward(context: TelegramContext, channelId: Long, channelName: String) extends TelegramMessage
  final case class Photo(context: TelegramContext, fileId: String) extends TelegramMessage
  final case class Unknown() extends TelegramMessage

  def fromRequest(message: TelegramWebhookRequest): TelegramMessage =
    message.message.map(parseMessage)
      .orElse(message.callbackQuery.map(parseCallback))
      .getOrElse(Unknown())

  private def parseMessage(message: TelegramMessageRequest): TelegramMessage = {
    val context = getContext(message)
    parseForward(message, context)
      .orElse(parsePhoto(message, context))
      .orElse(parseCommand(message, context))
      .orElse(parseText(message, context))
      .getOrElse(TelegramMessage.Unknown())
  }

  private def parseForward(message: TelegramMessageRequest, context: TelegramContext): Option[TelegramMessage] =
    message.forward.collect {
      case chat if chat.`type` == "channel" =>
        val name = chat.title.getOrElse("unknown channel")
        TelegramMessage.Forward(context, chat.id, name)
    }

  private def parsePhoto(message: TelegramMessageRequest, context: TelegramContext): Option[TelegramMessage] =
    message.photo.flatMap(_.lastOption).map(p => TelegramMessage.Photo(context, p.fileId))

  private def parseCommand(message: TelegramMessageRequest, context: TelegramContext): Option[TelegramMessage] =
    message.text.collect {
      case text if text.startsWith("/") =>
        TelegramMessage.Command(context, text)
    }

  private def parseText(message: TelegramMessageRequest, context: TelegramContext): Option[TelegramMessage] =
    message.text.map(text => TelegramMessage.Text(context, text))

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
