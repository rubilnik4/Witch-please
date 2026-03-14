package bot.application.handlers.telegram.flows

import bot.domain.models.session.{CardOfDayMode, CardOfDaySnapshot}
import bot.domain.models.session.pending.{BotPending, CardOfDayDraft, CardOfDayPending}
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.{BotSessionService, SessionRequire}
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.infrastructure.services.telegram.TelegramApiService
import zio.ZIO

object CardOfDayDraftFlow {
  def setCardOfDayStartDraft(context: TelegramContext, pending: CardOfDayPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)

      _ <- pending.draft match {
        case CardOfDayDraft.Start =>
          val nextDraft = CardOfDayDraft.AwaitingCardId
          val nextPending = CardOfDayPending(pending.mode, nextDraft)
          for {
            _ <- sessionService.setPending(context.chatId, BotPending.CardOfDay(nextPending))
            _ <- sendCardOfDayPendingReply(context, pending)
          } yield ()
        case _ =>
          ZIO.logError(s"Used card of day pending $pending instead of start draft chat=${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Used card of day pending $pending instead of start draft"))
      }
    } yield ()

  def setCardOfDayTextDraft(context: TelegramContext, text: String, pending: CardOfDayPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle text card of day draft ${pending.draft} from chat ${context.chatId}")

      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)

      (nextDraft, nextAction) <- pending.draft match {
        case CardOfDayDraft.Start | CardOfDayDraft.Complete(_, _, _, _) =>
          ZIO.logError(s"Couldn't used card of day start or complete pending in text draft chat=${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Couldn't used card of day start or complete pending in text draft"))
        case CardOfDayDraft.AwaitingCardId =>
          text.trim.toIntOption match {
            case Some(position) if position > 0 =>
              for {
                progress <- SessionRequire.spreadProgress(context.chatId)
                draftAction <- progress.createdPositions.find(_.position == position - 1) match {
                  case Some(createdPosition) =>
                    val nextDraft = CardOfDayDraft.AwaitingTitle(createdPosition.cardId)
                    val nextAction = sendCardOfDayPendingReply(context, pending)
                    ZIO.succeed(nextDraft -> nextAction)
                  case None =>
                    val nextAction =
                      ZIO.logInfo(s"Card with position ${position - 1} not found for chat ${context.chatId}") *>
                        telegramApi.sendText(context.chatId, "Карта с такой позицией ещё не создана. Введи другой номер")
                    ZIO.succeed(pending.draft -> nextAction)
                }
              } yield draftAction
            case _ =>
              val nextAction =
                ZIO.logInfo(s"Card position must be a positive integer for chat ${context.chatId}") *>
                  telegramApi.sendText(context.chatId, "Введи номер карты числом и больше 0")
              ZIO.succeed(pending.draft -> nextAction)
          }
        case CardOfDayDraft.AwaitingTitle(cardId) =>
          val nextDraft = CardOfDayDraft.AwaitingDescription(cardId, text)
          val nextPending = sendCardOfDayPendingReply(context, pending)
          ZIO.succeed(nextDraft -> nextPending)
        case CardOfDayDraft.AwaitingDescription(cardId, title) =>
          val nextDraft = CardOfDayDraft.AwaitingPhoto(cardId, title, text)
          val nextPending = sendCardOfDayPendingReply(context, pending)
          ZIO.succeed(nextDraft -> nextPending)
        case draft @ CardOfDayDraft.AwaitingPhoto(_,_,_) =>
          val nextPending =
            ZIO.logInfo(s"Used text instead of card of day photo in chat${context.chatId}") *>
              telegramApi.sendText(context.chatId, "Принимаю только фото!").unit
          ZIO.succeed(draft -> nextPending)
      }
      nextPending = CardOfDayPending(pending.mode, nextDraft)
      _ <- sessionService.setPending(context.chatId, BotPending.CardOfDay(nextPending))
      _ <- nextAction
    } yield ()

  def setCardOfDayPhotoDraft(context: TelegramContext, photoSourceId: String, pending: CardOfDayPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)

      _ <- pending.draft match {
        case CardOfDayDraft.AwaitingPhoto(cardId, title, description) =>
          val nextDraft = CardOfDayDraft.Complete(cardId, title, description, photoSourceId)
          val nextPending = CardOfDayPending(pending.mode, nextDraft)
          setCardOfDayCompleteDraft(context, nextPending)
        case _ =>
          ZIO.logInfo(s"Used photo instead of card of day text in chat ${context.chatId}") *>
            telegramApi.sendText(context.chatId, "Принимаю только текст!").unit
      }
    } yield ()

  private def setCardOfDayCompleteDraft(context: TelegramContext, pending: CardOfDayPending): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardOfDayDraft.Complete(cardId, title, description, photoSourceId) =>
        val snapshot = CardOfDaySnapshot(cardId, title, description, photoSourceId)
        CardOfDayFlow.submitCardOfDay(context, pending.mode, snapshot)
      case _ =>
        ZIO.logError(s"Used card of day pending $pending instead of complete draft chat=${context.chatId}") *>
          ZIO.fail(new IllegalStateException(s"Used card of day pending $pending instead of complete draft"))
    }

  private def sendCardOfDayPendingReply(context: TelegramContext, pending: CardOfDayPending) =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)

      (buttonText, currentValue) <- getCardOfDayPendingReply(context, pending)
      _ <- pending.mode match {
        case CardOfDayMode.Create =>
          telegramApi.sendReplyText(context.chatId, buttonText)
        case CardOfDayMode.Edit(_) =>
          CommonFlow.sendEditReply(context, buttonText, currentValue)
      }
    } yield ()

  private def getCardOfDayPendingReply(context: TelegramContext, pending: CardOfDayPending) =
    for {
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      session <- sessionService.get(context.chatId)

      (buttonText, currentValue) <- pending.draft match {
        case CardOfDayDraft.Start =>
          ZIO.succeed("Укажи номер карты дня для твоего расклада" -> session.cardOfDay.map(_.snapShot.cardId.toString))
        case CardOfDayDraft.AwaitingCardId =>
          ZIO.succeed("Укажи название карты дня" -> session.cardOfDay.map(_.snapShot.title))
        case CardOfDayDraft.AwaitingTitle(_) =>
          ZIO.succeed("Укажи подробное описание карты" -> session.cardOfDay.map(_.snapShot.description))
        case CardOfDayDraft.AwaitingDescription(_,_) =>
          ZIO.succeed("Прикрепи фото для карты" -> None)
        case CardOfDayDraft.AwaitingPhoto(_, _, _) =>
          ZIO.logError(s"Card of day called for AwaitingPhoto state in chat ${context.chatId}") *>
            ZIO.dieMessage("Card of day called for AwaitingPhoto state")
        case CardOfDayDraft.Complete(_, _, _, _) =>
          ZIO.logError(s"Card of day called for Complete state in chat ${context.chatId}") *>
            ZIO.dieMessage("Card of day called for Complete state")
      }
    } yield (buttonText, currentValue)  
}
