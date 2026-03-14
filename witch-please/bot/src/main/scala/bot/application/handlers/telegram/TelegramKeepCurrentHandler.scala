package bot.application.handlers.telegram

import bot.application.handlers.telegram.flows.*
import bot.domain.models.session.pending.*
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.SessionRequire
import bot.layers.BotEnv
import zio.ZIO

object TelegramKeepCurrentHandler {
  def handleKeepCurrent(context: TelegramContext): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      pending <- SessionRequire.pending(context.chatId)
      _ <- ZIO.logInfo(s"Received keep current command from chat ${context.chatId} for pending action $pending")

      _ <- pending match {
        case BotPending.Channel(pending) =>
          channelKeepCurrent(context, pending)
        case BotPending.Spread(pending) =>
          spreadKeepCurrent(context, pending)
        case BotPending.Card(pending) =>
          cardKeepCurrent(context, pending)
        case BotPending.CardOfDay(pending) =>
          cardOfDayKeepCurrent(context, pending)
      }
    } yield ()

  private def channelKeepCurrent(context: TelegramContext, pending: ChannelPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      _ <- ZIO.logError(s"Unknown keep current pending action $pending from chat ${context.chatId}")
      _ <- telegramApi.sendText(context.chatId, "Невозможно оставить текущее значение")
    } yield ()

  private def spreadKeepCurrent(context: TelegramContext, pending: SpreadPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      spread <- SessionRequire.spread(context.chatId)
      _ <- pending.draft match {
        case draft @ (SpreadDraft.Start | SpreadDraft.Complete(_,_,_,_)) =>
          ZIO.logError(s"Couldn't use spread $draft step to keep current in chat ${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Couldn't use spread $draft step to keep current"))
        case SpreadDraft.AwaitingTitle =>
          SpreadDraftFlow.setSpreadTextDraft(context, spread.snapShot.title, pending)
        case SpreadDraft.AwaitingCardsCount(_) =>
          SpreadDraftFlow.setSpreadTextDraft(context, spread.snapShot.cardsCount.toString, pending)
        case SpreadDraft.AwaitingDescription(_,_) =>
          SpreadDraftFlow.setSpreadTextDraft(context, spread.snapShot.description, pending)
        case draft @ SpreadDraft.AwaitingPhoto(_,_,_) =>
          SpreadDraftFlow.setSpreadPhotoDraft(context, spread.snapShot.photoSourceId, pending)
      }
    } yield ()

  private def cardKeepCurrent(context: TelegramContext, pending: CardPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      card <- SessionRequire.card(context.chatId)
      _ <- pending.draft match {
        case draft @ (CardDraft.Start | CardDraft.Complete(_,_,_)) =>
          ZIO.logError(s"Couldn't use card $draft step to keep current in chat ${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Couldn't use card $draft step to keep current"))
        case CardDraft.AwaitingTitle =>
          CardDraftFlow.setCardTextDraft(context, card.snapShot.title, pending)
        case CardDraft.AwaitingDescription(_) =>
          CardDraftFlow.setCardTextDraft(context, card.snapShot.description, pending)
        case draft @ CardDraft.AwaitingPhoto(_,_) =>
          CardDraftFlow.setCardPhotoDraft(context, card.snapShot.photoSourceId, pending)
      }
    } yield ()

  private def cardOfDayKeepCurrent(context: TelegramContext, pending: CardOfDayPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      card <- SessionRequire.card(context.chatId)
      _ <- pending.draft match {
        case draft @ (CardOfDayDraft.Start | CardOfDayDraft.Complete(_, _, _, _)) =>
          ZIO.logError(s"Couldn't use card of day $draft step to keep current in chat ${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Couldn't use card of day $draft step to keep current"))
        case CardOfDayDraft.AwaitingCardId =>
          CardOfDayDraftFlow.setCardOfDayTextDraft(context, card.snapShot.title, pending)
        case CardOfDayDraft.AwaitingTitle(_) =>
          CardOfDayDraftFlow.setCardOfDayTextDraft(context, card.snapShot.title, pending)
        case CardOfDayDraft.AwaitingDescription(_,_) =>
          CardOfDayDraftFlow.setCardOfDayTextDraft(context, card.snapShot.description, pending)
        case draft@CardOfDayDraft.AwaitingPhoto(_, _, _) =>
          CardOfDayDraftFlow.setCardOfDayPhotoDraft(context, card.snapShot.photoSourceId, pending)
      }
    } yield ()
}
