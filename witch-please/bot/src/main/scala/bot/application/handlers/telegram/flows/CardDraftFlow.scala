package bot.application.handlers.telegram.flows

import bot.domain.models.session.pending.{BotPending, CardDraft, CardPending}
import bot.domain.models.session.{CardMode, CardSnapshot}
import bot.domain.models.telegram.TelegramContext
import bot.layers.BotEnv
import zio.ZIO

import java.util.UUID

object CardDraftFlow {
  def setCardStartDraft(context: TelegramContext, pending: CardPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)

      _ <- pending.draft match {
        case CardDraft.Start =>
          val nextDraft = CardDraft.AwaitingTitle
          val nextPending = CardPending(pending.mode, nextDraft)
          for {
            _ <- sessionService.setPending(context.chatId, BotPending.Card(nextPending))
            _ <- sendCardPendingReply(context, pending)
          } yield ()
        case _ =>
          ZIO.logError(s"Used card pending $pending instead of start draft chat=${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Used card pending $pending instead of start draft"))
      }
    } yield ()

  def setCardTextDraft(context: TelegramContext, text: String, pending: CardPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle text card draft ${pending.draft} from chat ${context.chatId}")

      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)

      (nextDraft, nextAction) <- pending.draft match {
        case CardDraft.Start | CardDraft.Complete(_, _, _) =>
          ZIO.logError(s"Couldn't used card start or complete pending in text draft in chat ${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Couldn't used card start or complete pending in text draft"))
        case CardDraft.AwaitingTitle =>
          val nextDraft = CardDraft.AwaitingDescription(text)
          val nextPending = sendCardPendingReply(context, pending)
          ZIO.succeed(nextDraft -> nextPending)
        case CardDraft.AwaitingDescription(title) =>
          val nextDraft = CardDraft.AwaitingPhoto(title, text)
          val nextPending = sendCardPendingReply(context, pending)
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

  def setCardPhotoDraft(context: TelegramContext, photoSourceId: String, pending: CardPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      
      _ <- pending.draft match {
        case CardDraft.AwaitingPhoto(title, description) =>
          val nextDraft = CardDraft.Complete(title, description, photoSourceId)
          val nextPending = CardPending(pending.mode, nextDraft)
          setCardCompleteDraft(context, nextPending)
        case _ =>
          ZIO.logInfo(s"Used photo instead of card text in chat ${context.chatId}") *>
            telegramApi.sendText(context.chatId, "Принимаю только текст!").unit
      }
    } yield ()

  private def setCardCompleteDraft(context: TelegramContext, pending: CardPending): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardDraft.Complete(title, description, photoSourceId) =>
        val snapshot = CardSnapshot(title, description, photoSourceId)
        CardFlow.submitCard(context, pending.mode, snapshot)
      case _ =>
        ZIO.logError(s"Used card pending $pending instead of complete draft in chat ${context.chatId}") *>
          ZIO.fail(new IllegalStateException(s"Used card pending $pending instead of complete draft"))
    }

  private def sendCardPendingReply(context: TelegramContext, pending: CardPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      
      (buttonText, currentValue) <- getCardPendingReply(context, pending)
      _ <- pending.mode match {
        case CardMode.Create(_) =>
          telegramApi.sendReplyText(context.chatId, buttonText)
        case CardMode.Edit(_) =>
          CommonFlow.sendEditReply(context, buttonText, currentValue)
      }
    } yield ()

  private def getCardPendingReply(context: TelegramContext, pending: CardPending) =
    for {
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      
      session <- sessionService.get(context.chatId)
      (buttonText, currentValue) <- pending.draft match {
        case CardDraft.Start =>
          ZIO.succeed("Напиши название карты" -> session.card.map(_.snapShot.title))
        case CardDraft.AwaitingTitle =>
          ZIO.succeed("Укажи подробное описание карты" -> session.card.map(_.snapShot.description))
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
