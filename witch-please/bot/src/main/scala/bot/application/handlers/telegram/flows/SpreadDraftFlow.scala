package bot.application.handlers.telegram.flows

import bot.domain.models.session.pending.{BotPending, SpreadDraft, SpreadPending}
import bot.domain.models.session.{SpreadMode, SpreadSnapshot}
import bot.domain.models.telegram.TelegramContext
import bot.layers.BotEnv
import zio.ZIO

object SpreadDraftFlow {
  def setSpreadStartDraft(context: TelegramContext, pending: SpreadPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)

      _ <- pending.draft match {
        case SpreadDraft.Start =>
          val nextDraft = SpreadDraft.AwaitingTitle
          val nextPending = SpreadPending(pending.mode, nextDraft)
          for {
            _ <- sessionService.setPending(context.chatId, BotPending.Spread(nextPending))
            _ <- sendSpreadPendingReply(context, pending)
          } yield ()
        case _ =>
          ZIO.logError(s"Used spread pending $pending instead of start draft chat=${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Used spread pending $pending instead of start draft"))
      }
    } yield ()

  def setSpreadTextDraft(context: TelegramContext, text: String, pending: SpreadPending): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle text spread draft ${pending.draft} from chat ${context.chatId}")

      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)

      (nextDraft, nextAction) <- pending.draft match {
        case SpreadDraft.Start | SpreadDraft.Complete(_,_,_,_) =>
          ZIO.logError(s"Couldn't used spread start or complete pending in text draft chat=${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Couldn't used spread start or complete  pending in text draft"))
        case SpreadDraft.AwaitingTitle =>
          val nextDraft = SpreadDraft.AwaitingCardsCount(text)
          val nextPending = sendSpreadPendingReply(context, pending)
          ZIO.succeed(nextDraft -> nextPending)
        case SpreadDraft.AwaitingCardsCount(title) =>
          text.trim.toIntOption match {
            case Some(cardsCount) if cardsCount > 0 =>
              val nextDraft = SpreadDraft.AwaitingDescription(title, cardsCount)
              val nextAction = sendSpreadPendingReply(context, pending)
              ZIO.succeed(nextDraft -> nextAction)
            case _ =>
              val nextAction =
                ZIO.logInfo(s"Cards count must be greater than 0 for chat ${context.chatId}") *>
                  telegramApi.sendText(context.chatId, "Число карт должно быть больше 0. Введи число ещё раз.")
              ZIO.succeed(pending.draft -> nextAction)
          }
        case SpreadDraft.AwaitingDescription(title, cardCount) =>
          val nextDraft = SpreadDraft.AwaitingPhoto(title, cardCount, text)
          val nextPending = sendSpreadPendingReply(context, pending)
          ZIO.succeed(nextDraft -> nextPending)
        case draft @ SpreadDraft.AwaitingPhoto(_,_,_) =>
          val nextPending =
            ZIO.logInfo(s"Used text instead of spread photo in chat ${context.chatId}") *>
              telegramApi.sendText(context.chatId, "Принимаю только фото!").unit
          ZIO.succeed(draft -> nextPending)
      }
      nextPending = SpreadPending(pending.mode, nextDraft)
      _ <- sessionService.setPending(context.chatId, BotPending.Spread(nextPending))
      _ <- nextAction
    } yield ()

  def setSpreadPhotoDraft(context: TelegramContext, photoSourceId: String, pending: SpreadPending): ZIO[BotEnv, Throwable, Unit] = {
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)

      _ <- pending.draft match {
        case SpreadDraft.AwaitingPhoto(title, cardsCount, description) =>
          val nextDraft = SpreadDraft.Complete(title, cardsCount, description, photoSourceId)
          val nextPending = SpreadPending(pending.mode, nextDraft)
          setSpreadCompleteDraft(context, nextPending)
        case _ =>
          ZIO.logInfo(s"Used photo instead of spread text in chat ${context.chatId}") *>
            telegramApi.sendText(context.chatId, "Принимаю только текст!").unit
      }
    } yield ()
  }

  private def setSpreadCompleteDraft(context: TelegramContext, pending: SpreadPending) =
    pending.draft match {
      case SpreadDraft.Complete(title, cardsCount, description, photoSourceId) =>
        val snapshot = SpreadSnapshot(title, cardsCount, description, photoSourceId)
        SpreadFlow.submitSpread(context, pending.mode, snapshot)
      case _ =>
        ZIO.logError(s"Used spread pending $pending instead of complete draft in chat ${context.chatId}") *>
          ZIO.fail(new IllegalStateException(s"Used spread pending $pending instead of complete draft"))
    }

  private def sendSpreadPendingReply(context: TelegramContext, pending: SpreadPending) =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)

      (buttonText, currentValue) <- getSpreadPendingReply(context, pending)
      _ <- pending.mode match {
        case SpreadMode.Create =>
          telegramApi.sendReplyText(context.chatId, buttonText)
        case SpreadMode.Edit(_) =>
          CommonFlow.sendEditReply(context, buttonText, currentValue)
      }
    } yield ()

  private def getSpreadPendingReply(context: TelegramContext, pending: SpreadPending) =
    for {
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)

      session <- sessionService.get(context.chatId)
      (buttonText, currentValue) <- pending.draft match {
        case SpreadDraft.Start =>
          ZIO.succeed("Напиши название расклада" -> session.spread.map(_.snapShot.title))
        case SpreadDraft.AwaitingTitle =>
          ZIO.succeed("Укажи количество карт в раскладе" -> session.spread.map(_.snapShot.cardsCount.toString))
        case SpreadDraft.AwaitingCardsCount(_) =>
          ZIO.succeed("Укажи подробное описание расклада" -> session.spread.map(_.snapShot.description))
        case SpreadDraft.AwaitingDescription(_,_) =>
          ZIO.succeed("Прикрепи фото для расклада" -> None)
        case SpreadDraft.AwaitingPhoto(_,_,_) =>
          ZIO.logError(s"Spread called for AwaitingPhoto state chat=${context.chatId}") *>
            ZIO.dieMessage("Spread called for AwaitingPhoto state")
        case SpreadDraft.Complete(_,_,_,_) =>
          ZIO.logError(s"Spread called for Complete state chat=${context.chatId}") *>
            ZIO.dieMessage("Spread called for Complete state")
      }
    } yield (buttonText, currentValue)
}
