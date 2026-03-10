package bot.application.handlers.telegram

import bot.application.handlers.telegram.flows.*
import bot.domain.models.session.pending.SpreadDraft.AwaitingTitle
import bot.domain.models.session.pending.{BotPending, SpreadDraft, SpreadPending}
import bot.domain.models.telegram.*
import bot.infrastructure.services.sessions.{BotSessionService, SessionRequire}
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.infrastructure.services.telegram.TelegramApiService
import zio.*

object TelegramTextHandler {
  def handle(context: TelegramContext, text: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)

      pending <- SessionRequire.pending(context.chatId)
      _ <- ZIO.logInfo(s"Received text from chat ${context.chatId} for pending action $pending")
      
      _ <- pending match {
        case BotPending.Spread(pending) =>
          handleSpread(context, text, pending)(telegramApi, tarotApi, sessionService)
        case BotPending.CardTitle(cardMode) =>
          CardFlow.setCardTitle(context, cardMode, text)(telegramApi, tarotApi, sessionService)
        case BotPending.CardDescription(cardMode, title) =>
          CardFlow.setCardDescription(context, cardMode, title, text)(telegramApi, tarotApi, sessionService)
        case BotPending.CardOfDayCardId(cardOfDayMode) =>
          text.toIntOption match {
            case Some(position) if position > 0 =>
              CardOfDayFlow.setCardOfDayCardId(context, cardOfDayMode, position - 1)(telegramApi, tarotApi, sessionService)
            case _ =>
              telegramApi.sendText(context.chatId, "Введи номер карты числом и больше 0")
          }
        case BotPending.CardOfDayTitle(cardMode, cardId) =>
          CardOfDayFlow.setCardOfDayTitle(context, cardMode, cardId, text)(telegramApi, tarotApi, sessionService)
        case BotPending.CardOfDayDescription(cardMode, cardId, title) =>
          CardOfDayFlow.setCardOfDayDescription(context, cardMode, cardId, title, text)(telegramApi, tarotApi, sessionService)
        case BotPending.ChannelChannelId(_) =>
          for {
            _ <- ZIO.logInfo(s"Ignored plain text from ${context.chatId}: $text")
            _ <- telegramApi.sendText(context.chatId, "Используйте команды. Введите /help.")
          } yield ()
        case BotPending.CardPhoto(_,_,_) | BotPending.CardOfDayPhoto(_,_,_,_) =>
          for {
            _ <- ZIO.logInfo(s"Used text message instead of photo ${context.chatId}: $text")
            _ <- telegramApi.sendText(context.chatId, "Принимаю только фото!")
          } yield ()
      }
    } yield ()

  private def handleSpread(context: TelegramContext, text: String, pending: SpreadPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case SpreadDraft.AwaitingTitle
           | SpreadDraft.AwaitingCardsCount(_)
           | SpreadDraft.AwaitingDescription(_,_)
      =>
        SpreadFlow.setSpreadTextDraft(context, text, pending)(telegramApi, tarotApi, sessionService)
      case SpreadDraft.Start | SpreadDraft.AwaitingPhoto(_,_,_) | SpreadDraft.Complete(_,_,_,_) =>
        for {
          _ <- ZIO.logWarning(s"Used text message instead of ${pending.draft} in chat ${context.chatId}")
          _ <- telegramApi.sendText(context.chatId, "Принимаю только текст!")
        } yield ()
    }
}