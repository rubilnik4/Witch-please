package bot.application.handlers.telegram

import bot.application.handlers.telegram.TelegramTextHandler.handleSpread
import bot.application.handlers.telegram.flows.*
import bot.domain.models.session.pending.{BotPending, SpreadDraft, SpreadPending}
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.{BotSessionService, SessionRequire}
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.infrastructure.services.telegram.TelegramApiService
import zio.ZIO

object TelegramPhotoHandler {
  def handle(context: TelegramContext, sourceId: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)

      pending <- SessionRequire.pending(context.chatId)
      _ <- ZIO.logInfo(s"Received photo from chat ${context.chatId} for pending action $pending")
      
      _ <- pending match {      
        case BotPending.Spread(pending) =>
          handleSpread(context, sourceId, pending)(telegramApi, tarotApi, sessionService)
        case BotPending.CardPhoto(cardMode, title, description) =>
          CardFlow.setCardPhoto(context, cardMode,  title, description, sourceId)(telegramApi, tarotApi, sessionService)
        case BotPending.CardOfDayPhoto(cardOfDayMode, cardId, title, description) =>
          CardOfDayFlow.setCardOfDayPhoto(context, cardOfDayMode, cardId, title, description, sourceId)(telegramApi, tarotApi, sessionService)  
        case BotPending.ChannelChannelId(_) 
             | BotPending.CardTitle(_) | BotPending.CardDescription(_,_)
             | BotPending.CardOfDayCardId(_) | BotPending.CardOfDayTitle(_,_)
             | BotPending.CardOfDayDescription(_,_,_)
        =>
          for {
            _ <- ZIO.logError(s"Unknown photo pending action ${pending} from chat ${context.chatId}")
            _ <- telegramApi.sendText(context.chatId, "Неизвестная команда отправки фото")
          } yield ()
      }
    } yield ()

  private def handleSpread(context: TelegramContext, sourceId: String, pending: SpreadPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case SpreadDraft.AwaitingPhoto(_,_,_) =>
        SpreadFlow.setSpreadPhotoDraft(context, sourceId, pending)(telegramApi, tarotApi, sessionService)
      case SpreadDraft.Start
           | SpreadDraft.AwaitingTitle
           | SpreadDraft.AwaitingCardsCount(_)
           | SpreadDraft.AwaitingDescription(_,_)
           | SpreadDraft.Complete(_,_,_,_) =>
        for {
          _ <- ZIO.logInfo(s"Used photo message instead of ${pending.draft} in chat ${context.chatId}")
          _ <- telegramApi.sendText(context.chatId, "Принимаю только фото!")
        } yield ()
    }
}
