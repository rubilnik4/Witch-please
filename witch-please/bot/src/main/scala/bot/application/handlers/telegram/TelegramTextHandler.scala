package bot.application.handlers.telegram

import bot.application.handlers.telegram.flows.*
import bot.domain.models.session.pending.SpreadDraft.AwaitingTitle
import bot.domain.models.session.pending.{BotPending, CardDraft, CardOfDayDraft, CardOfDayPending, CardPending, SpreadDraft, SpreadPending}
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
        case BotPending.Card(pending) =>
          handleCard(context, text, pending)(telegramApi, tarotApi, sessionService)     
        case BotPending.CardOfDay(pending) =>
          handleCardOfDay(context, text, pending)(telegramApi, tarotApi, sessionService)
        case BotPending.ChannelChannelId(_) =>
          for {
            _ <- ZIO.logError(s"Unexpected plain text for ChannelChannelId state int chat ${context.chatId}")
            _ <- telegramApi.sendText(context.chatId, "Неизвестная команда сообщения каналов. Введите /help")
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
        SpreadDraftFlow.setSpreadTextDraft(context, text, pending)(telegramApi, tarotApi, sessionService)
      case SpreadDraft.Start | SpreadDraft.AwaitingPhoto(_,_,_) | SpreadDraft.Complete(_,_,_,_) =>
        for {
          _ <- ZIO.logWarning(s"Used spread text message instead of ${pending.draft} in chat ${context.chatId}")
          _ <- telegramApi.sendText(context.chatId, "Принимаю только текст!")
        } yield ()
    }

  private def handleCard(context: TelegramContext, text: String, pending: CardPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardDraft.AwaitingTitle | CardDraft.AwaitingDescription(_) =>
        CardDraftFlow.setCardTextDraft(context, text, pending)(telegramApi, tarotApi, sessionService)
      case CardDraft.Start | CardDraft.AwaitingPhoto(_, _) | CardDraft.Complete(_, _, _) =>
        for {
          _ <- ZIO.logWarning(s"Used card text message instead of ${pending.draft} in chat ${context.chatId}")
          _ <- telegramApi.sendText(context.chatId, "Принимаю только текст!")
        } yield ()
    }

  private def handleCardOfDay(context: TelegramContext, text: String, pending: CardOfDayPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardOfDayDraft.AwaitingCardId | CardOfDayDraft.AwaitingTitle(_) | CardOfDayDraft.AwaitingDescription(_,_) =>
        CardOfDayDraftFlow.setCardOfDayTextDraft(context, text, pending)(telegramApi, tarotApi, sessionService)
      case CardOfDayDraft.Start | CardOfDayDraft.AwaitingPhoto(_,_,_) | CardOfDayDraft.Complete(_,_,_,_) =>
        for {
          _ <- ZIO.logWarning(s"Used card of day text message instead of ${pending.draft} in chat ${context.chatId}")
          _ <- telegramApi.sendText(context.chatId, "Принимаю только текст!")
        } yield ()
    }  
}