package bot.application.handlers.telegram

import bot.application.handlers.telegram.flows.SpreadFlow
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
      session <- sessionService.get(context.chatId)
      _ <- ZIO.logInfo(s"Received keep current command from chat ${context.chatId} for pending action ${session.pending}")

      _ <- session.pending match {
        case Some(BotPending.Spread(spreadPending)) =>
          spreadKeepCurrent(context, spreadPending)(telegramApi, tarotApi, sessionService)
        case None
             | Some(BotPending.ChannelChannelId(_))
             | Some(BotPending.CardTitle(_)) | Some(BotPending.CardDescription(_,_))
             | Some(BotPending.CardOfDayCardId(_)) | Some(BotPending.CardOfDayTitle(_,_))
             | Some(BotPending.CardOfDayDescription(_,_,_))
             | Some(BotPending.CardPhoto(_,_,_))
             | Some(BotPending.CardOfDayPhoto(_,_,_,_))
        =>
          for {
            _ <- ZIO.logError(s"Unknown keep current pending action ${session.pending} from chat ${context.chatId}")
            _ <- telegramApi.sendText(context.chatId, "Невозможно оставить текущее значение")
          } yield ()
      }
    } yield ()

  private def spreadKeepCurrent(context: TelegramContext, pending: SpreadPending)
      (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      spread <- SessionRequire.spread(context.chatId)
      _ <- pending.draft match {
        case draft @ (SpreadDraft.Start | SpreadDraft.Complete(_,_,_,_)) =>
          ZIO.logError(s"Couldn't use $draft step to keep current in chat ${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Couldn't use $draft step to keep current"))
        case SpreadDraft.AwaitingTitle =>
          SpreadFlow.setSpreadTextDraft(context, spread.snapShot.title, pending)(telegramApi, tarotApi, sessionService)
        case SpreadDraft.AwaitingCardsCount(_) =>
          SpreadFlow.setSpreadTextDraft(context, spread.snapShot.cardsCount.toString, pending)(telegramApi, tarotApi, sessionService)
        case SpreadDraft.AwaitingDescription(_,_) =>
          SpreadFlow.setSpreadPhotoDraft(context, spread.snapShot.description, pending)(telegramApi, tarotApi, sessionService)
        case draft @ SpreadDraft.AwaitingPhoto(_,_,_) =>
          SpreadFlow.setSpreadPhotoDraft(context, spread.snapShot.photoSourceId, pending)(telegramApi, tarotApi, sessionService)
      }
    } yield ()
}
