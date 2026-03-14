package bot.application.handlers.telegram

import bot.application.handlers.telegram.flows.{CardDraftFlow, CardFlow, CardOfDayFlow, SpreadFlow}
import bot.application.handlers.telegram.flows.SpreadFlow.*
import bot.domain.models.session.pending.*
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.{BotSessionService, SessionRequire}
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.infrastructure.services.telegram.TelegramApiService
import zio.ZIO

object TelegramKeepCurrentHandler {
  def handleKeepCurrent(context: TelegramContext)
    (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      pending <- SessionRequire.pending(context.chatId)
      _ <- ZIO.logInfo(s"Received keep current command from chat ${context.chatId} for pending action $pending")

      _ <- pending match {
        case BotPending.Spread(pending) =>
          spreadKeepCurrent(context, pending)(telegramApi, tarotApi, sessionService)
        case BotPending.Card(pending) =>
          cardKeepCurrent(context, pending)(telegramApi, tarotApi, sessionService)
        case BotPending.CardOfDay(pending) =>
          cardOfDayKeepCurrent(context, pending)(telegramApi, tarotApi, sessionService)
        case BotPending.ChannelChannelId(_) =>
          for {
            _ <- ZIO.logError(s"Unknown keep current pending action $pending from chat ${context.chatId}")
            _ <- telegramApi.sendText(context.chatId, "Невозможно оставить текущее значение")
          } yield ()
      }
    } yield ()

  private def spreadKeepCurrent(context: TelegramContext, pending: SpreadPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      spread <- SessionRequire.spread(context.chatId)
      _ <- pending.draft match {
        case draft @ (SpreadDraft.Start | SpreadDraft.Complete(_,_,_,_)) =>
          ZIO.logError(s"Couldn't use spread $draft step to keep current in chat ${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Couldn't use spread $draft step to keep current"))
        case SpreadDraft.AwaitingTitle =>
          SpreadFlow.setSpreadTextDraft(context, spread.snapShot.title, pending)(telegramApi, tarotApi, sessionService)
        case SpreadDraft.AwaitingCardsCount(_) =>
          SpreadFlow.setSpreadTextDraft(context, spread.snapShot.cardsCount.toString, pending)(telegramApi, tarotApi, sessionService)
        case SpreadDraft.AwaitingDescription(_,_) =>
          SpreadFlow.setSpreadTextDraft(context, spread.snapShot.description, pending)(telegramApi, tarotApi, sessionService)
        case draft @ SpreadDraft.AwaitingPhoto(_,_,_) =>
          SpreadFlow.setSpreadPhotoDraft(context, spread.snapShot.photoSourceId, pending)(telegramApi, tarotApi, sessionService)
      }
    } yield ()

  private def cardKeepCurrent(context: TelegramContext, pending: CardPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      card <- SessionRequire.card(context.chatId)
      _ <- pending.draft match {
        case draft @ (CardDraft.Start | CardDraft.Complete(_,_,_)) =>
          ZIO.logError(s"Couldn't use card $draft step to keep current in chat ${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Couldn't use card $draft step to keep current"))
        case CardDraft.AwaitingTitle =>
          CardDraftFlow.setCardTextDraft(context, card.snapShot.title, pending)(telegramApi, tarotApi, sessionService)       
        case CardDraft.AwaitingDescription(_) =>
          CardDraftFlow.setCardTextDraft(context, card.snapShot.description, pending)(telegramApi, tarotApi, sessionService)
        case draft @ CardDraft.AwaitingPhoto(_,_) =>
          CardDraftFlow.setCardPhotoDraft(context, card.snapShot.photoSourceId, pending)(telegramApi, tarotApi, sessionService)
      }
    } yield ()

  private def cardOfDayKeepCurrent(context: TelegramContext, pending: CardOfDayPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      card <- SessionRequire.card(context.chatId)
      _ <- pending.draft match {
        case draft @ (CardOfDayDraft.Start | CardOfDayDraft.Complete(_, _, _, _)) =>
          ZIO.logError(s"Couldn't use card of day $draft step to keep current in chat ${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Couldn't use card of day $draft step to keep current"))
        case CardOfDayDraft.AwaitingCardId =>
          CardOfDayFlow.setCardOfDayTextDraft(context, card.snapShot.title, pending)(telegramApi, tarotApi, sessionService)
        case CardOfDayDraft.AwaitingTitle(_) =>
          CardOfDayFlow.setCardOfDayTextDraft(context, card.snapShot.title, pending)(telegramApi, tarotApi, sessionService)
        case CardOfDayDraft.AwaitingDescription(_,_) =>
          CardOfDayFlow.setCardOfDayTextDraft(context, card.snapShot.description, pending)(telegramApi, tarotApi, sessionService)
        case draft@CardOfDayDraft.AwaitingPhoto(_, _, _) =>
          CardOfDayFlow.setCardOfDayPhotoDraft(context, card.snapShot.photoSourceId, pending)(telegramApi, tarotApi, sessionService)
      }
    } yield ()
}
