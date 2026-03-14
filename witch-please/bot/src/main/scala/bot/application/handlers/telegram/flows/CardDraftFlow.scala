package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.{AuthorCommands, TelegramCommands}
import bot.domain.models.session.pending.{BotPending, CardDraft, CardPending}
import bot.domain.models.session.{BotCard, BotSpread, CardMode, CardSnapshot}
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.{BotSessionService, SessionRequire}
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.cards.*
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.tarot.cards.CardPosition
import shared.models.tarot.spreads.SpreadStatus
import zio.ZIO

import java.util.UUID

object CardDraftFlow {
  def setCardStartDraft(context: TelegramContext, pending: CardPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardDraft.Start =>
        val nextDraft = CardDraft.AwaitingTitle
        val nextPending = CardPending(pending.mode, nextDraft)
        for {
          _ <- sessionService.setPending(context.chatId, BotPending.Card(nextPending))
          _ <- sendCardPendingReply(context, pending)(telegramApi, tarotApi, sessionService)
        } yield ()
      case _ =>
        ZIO.logError(s"Used card pending $pending instead of start draft chat=${context.chatId}") *>
          ZIO.fail(new IllegalStateException(s"Used card pending $pending instead of start draft"))
    }

  def setCardTextDraft(context: TelegramContext, text: String, pending: CardPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle text card draft ${pending.draft} from chat ${context.chatId}")

      (nextDraft, nextAction) <- pending.draft match {
        case CardDraft.Start | CardDraft.Complete(_, _, _) =>
          ZIO.logError(s"Couldn't used card start or complete pending in text draft in chat ${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Couldn't used card start or complete pending in text draft"))
        case CardDraft.AwaitingTitle =>
          val nextDraft = CardDraft.AwaitingDescription(text)
          val nextPending = sendCardPendingReply(context, pending)(telegramApi, tarotApi, sessionService)
          ZIO.succeed(nextDraft -> nextPending)
        case CardDraft.AwaitingDescription(title) =>
          val nextDraft = CardDraft.AwaitingPhoto(title, text)
          val nextPending = sendCardPendingReply(context, pending)(telegramApi, tarotApi, sessionService)
          ZIO.succeed(nextDraft -> nextPending)
        case draft@CardDraft.AwaitingPhoto(_, _) =>
          val nextPending =
            ZIO.logInfo(s"Used text instead of card photo in chat ${context.chatId}") *>
              telegramApi.sendText(context.chatId, "Принимаю только фото!").unit
          ZIO.succeed(draft -> nextPending)
      }
      nextPending = CardPending(pending.mode, nextDraft)
      _ <- sessionService.setPending(context.chatId, BotPending.Card(nextPending))
      _ <- nextAction
    } yield ()

  def setCardPhotoDraft(context: TelegramContext, photoSourceId: String, pending: CardPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardDraft.AwaitingPhoto(title, description) =>
        val nextDraft = CardDraft.Complete(title, description, photoSourceId)
        val nextPending = CardPending(pending.mode, nextDraft)
        setCardCompleteDraft(context, nextPending)(telegramApi, tarotApi, sessionService)
      case _ =>
        ZIO.logInfo(s"Used photo instead of card text in chat ${context.chatId}") *>
          telegramApi.sendText(context.chatId, "Принимаю только текст!").unit
    }

  private def setCardCompleteDraft(context: TelegramContext, pending: CardPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardDraft.Complete(title, description, photoSourceId) =>
        val snapshot = CardSnapshot(title, description, photoSourceId)
        CardFlow.submitCard(context, pending.mode, snapshot)(telegramApi, tarotApi, sessionService)
      case _ =>
        ZIO.logError(s"Used card pending $pending instead of complete draft in chat ${context.chatId}") *>
          ZIO.fail(new IllegalStateException(s"Used card pending $pending instead of complete draft"))
    }

  private def sendCardPendingReply(context: TelegramContext, pending: CardPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      (buttonText, currentValue) <- getCardPendingReply(context, pending)(sessionService)
      _ <- pending.mode match {
        case CardMode.Create(_) =>
          telegramApi.sendReplyText(context.chatId, buttonText)
        case CardMode.Edit(_) =>
          CommonFlow.sendEditReply(context, buttonText, currentValue)(telegramApi, tarotApi, sessionService)
      }
    } yield ()

  private def getCardPendingReply(context: TelegramContext, pending: CardPending)(sessionService: BotSessionService) =
    for {
      session <- sessionService.get(context.chatId)
      (buttonText, currentValue) <- pending.draft match {
        case CardDraft.Start =>
          ZIO.succeed("Напиши название карты" -> session.card.map(_.snapShot.title))
        case CardDraft.AwaitingTitle =>
          ZIO.succeed("Укажи подробное описание карты" -> session.card.map(_.snapShot.description.toString))
        case CardDraft.AwaitingDescription(_) =>
          ZIO.succeed("Прикрепи фото для карты" -> None)
        case CardDraft.AwaitingPhoto(_,_) =>
          ZIO.logError(s"Card called for AwaitingPhoto state chat=${context.chatId}") *>
            ZIO.dieMessage("Card called for AwaitingPhoto state")
        case CardDraft.Complete(_,_,_) =>
          ZIO.logError(s"Card called for Complete state chat=${context.chatId}") *>
            ZIO.dieMessage("Card called for Complete state")
      }
    } yield (buttonText, currentValue)  
}
