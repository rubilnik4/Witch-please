package bot.application.handlers.telegram

import bot.application.handlers.telegram.flows.*
import bot.domain.models.session.pending.{BotPending, CardDraft, CardOfDayDraft, CardOfDayPending, CardPending, ChannelPending, SpreadDraft, SpreadPending}
import bot.domain.models.telegram.*
import bot.infrastructure.services.sessions.SessionRequire
import bot.layers.BotEnv
import zio.*

object TelegramTextHandler {
  def handle(context: TelegramContext, text: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      pending <- SessionRequire.pending(context.chatId)
      
      _ <- ZIO.logInfo(s"Received text from chat ${context.chatId} for pending action $pending")
      
      _ <- pending match {
        case BotPending.Channel(pending) =>
          handleChannel(context, pending)
        case BotPending.Spread(pending) =>
          handleSpread(context, text, pending)
        case BotPending.Card(pending) =>
          handleCard(context, text, pending)
        case BotPending.CardOfDay(pending) =>
          handleCardOfDay(context, text, pending)
      }
    } yield ()

  private def handleChannel(context: TelegramContext, pending: ChannelPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      _ <- ZIO.logError(s"Unexpected plain text for channel state $pending in chat ${context.chatId}")
      _ <- telegramApi.sendText(context.chatId, "Неизвестная команда сообщения каналов. Введите /help")
    } yield ()

  private def handleSpread(context: TelegramContext, text: String, pending: SpreadPending): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case SpreadDraft.AwaitingTitle
           | SpreadDraft.AwaitingCardsCount(_)
           | SpreadDraft.AwaitingDescription(_,_)
      =>
        SpreadDraftFlow.setSpreadTextDraft(context, text, pending)
      case SpreadDraft.Start | SpreadDraft.AwaitingPhoto(_,_,_) | SpreadDraft.Complete(_,_,_,_) =>
        for {
          telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
          _ <- ZIO.logWarning(s"Used spread text message instead of ${pending.draft} in chat ${context.chatId}")
          _ <- telegramApi.sendText(context.chatId, "Принимаю только текст!")
        } yield ()
    }

  private def handleCard(context: TelegramContext, text: String, pending: CardPending): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardDraft.AwaitingTitle | CardDraft.AwaitingDescription(_) =>
        CardDraftFlow.setCardTextDraft(context, text, pending)
      case CardDraft.Start | CardDraft.AwaitingPhoto(_, _) | CardDraft.Complete(_, _, _) =>
        for {
          telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
          _ <- ZIO.logWarning(s"Used card text message instead of ${pending.draft} in chat ${context.chatId}")
          _ <- telegramApi.sendText(context.chatId, "Принимаю только текст!")
        } yield ()
    }

  private def handleCardOfDay(context: TelegramContext, text: String, pending: CardOfDayPending): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardOfDayDraft.AwaitingCardId | CardOfDayDraft.AwaitingTitle(_) | CardOfDayDraft.AwaitingDescription(_,_) =>
        CardOfDayDraftFlow.setCardOfDayTextDraft(context, text, pending)
      case CardOfDayDraft.Start | CardOfDayDraft.AwaitingPhoto(_,_,_) | CardOfDayDraft.Complete(_,_,_,_) =>
        for {
          telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
          _ <- ZIO.logWarning(s"Used card of day text message instead of ${pending.draft} in chat ${context.chatId}")
          _ <- telegramApi.sendText(context.chatId, "Принимаю только текст!")
        } yield ()
    }  
}
