package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.{AuthorCommands, TelegramCommands}
import bot.domain.models.session.*
import bot.domain.models.session.pending.{BotPending, CardOfDayDraft, CardOfDayPending}
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.datetime.DateFormatter
import bot.infrastructure.services.sessions.{BotSessionService, SessionRequire}
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.cardsOfDay.*
import shared.api.dto.tarot.photo.PhotoRequest
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.files.FileSourceType
import shared.models.tarot.spreads.SpreadStatus
import zio.ZIO

import java.util.UUID

object CardOfDayFlow {
  def selectCardOfDay(context: TelegramContext, spreadId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Select card of day by spread $spreadId from chat ${context.chatId}")

      token <- SessionRequire.token(context.chatId)
     
      cardOfDayMaybe <- tarotApi.getCardOfDayBySpread(spreadId, token)
      _ <- cardOfDayMaybe match {
        case None =>
          createCardOfDay(context)(telegramApi, tarotApi, sessionService)
        case Some(cardOfDay) =>
          showSpreadCardOfDay(context, cardOfDay, spreadId)(telegramApi, tarotApi, sessionService)
      }
    } yield ()

  private def showSpreadCardOfDay(context: TelegramContext, cardOfDay: CardOfDayResponse, spreadId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      spread <- SessionRequire.spread(context.chatId)

      _ <- sessionService.setCardOfDay(context.chatId, cardOfDay.id)
      _ <- showCardOfDay(context, cardOfDay, spread)(telegramApi, sessionService)
    } yield ()

  def createCardOfDay(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Create card of day for chat ${context.chatId}")

      _ <- startCardOfDayPending(context, CardOfDayMode.Create)(telegramApi, tarotApi, sessionService)
    } yield ()

  def editCardOfDay(context: TelegramContext, cardOfDayId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Edit card of day $cardOfDayId for chat ${context.chatId}")

      _ <- startCardOfDayPending(context, CardOfDayMode.Edit(cardOfDayId))(telegramApi, tarotApi, sessionService)
    } yield ()

  def deleteCardOfDay(context: TelegramContext, cardOfDayId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Delete card of day $cardOfDayId for chat ${context.chatId}")

      spread <- SessionRequire.spread(context.chatId)
      token <- SessionRequire.token(context.chatId)

      _ <- tarotApi.deleteCardOfDay(cardOfDayId, token)
      _ <- telegramApi.sendText(context.chatId, s"Карта дня удалена")
      _ <- sessionService.clearCardOfDay(context.chatId)

      _ <- SpreadFlow.selectSpread(context, spread.spreadId)(telegramApi, tarotApi, sessionService)
    } yield ()

//  def setCardOfDayCardId(context: TelegramContext, cardOfDayMode: CardOfDayMode, position: Int)(
//    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
//    for {
//      progress <- SessionRequire.spreadProgress(context.chatId)
//      _ <-
//        if (progress.createdPositions.exists(_.position == position))
//          for {
//            _ <- ZIO.logInfo(s"Handle card id to card of day from chat ${context.chatId}")
//
//            cardId <- ZIO.fromOption(progress.createdPositions.find(_.position == position).map(_.cardId))
//              .orElseFail(new RuntimeException("Card id in spread progress not found"))
//            _ <- sessionService.setPending(context.chatId, BotPending.CardOfDayTitle(cardOfDayMode, cardId))
//            _ <- telegramApi.sendReplyText(context.chatId, s"Укажи название карты дня")
//          } yield ()
//        else {
//          for {
//            spread <- SessionRequire.spread(context.chatId)
//            _ <- sessionService.clearCardOfDay(context.chatId)
//            _ <- telegramApi.sendText(context.chatId, "Карта с такой позицией ещё не создана")
//            _ <- SpreadFlow.selectSpread(context, spread.spreadId)(telegramApi, tarotApi, sessionService)
//          } yield ()
//        }
//    } yield ()
//
//  def setCardOfDayTitle(context: TelegramContext, cardOfDayMode: CardOfDayMode, cardId: UUID, title: String)(
//    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
//    for {
//      _ <- ZIO.logInfo(s"Handle card of day title from chat ${context.chatId}")
//
//      _ <- sessionService.setPending(context.chatId, BotPending.CardOfDayDescription(cardOfDayMode, cardId, title))
//      _ <- telegramApi.sendReplyText(context.chatId, s"Укажи подробное описание карты дня")
//    } yield ()  
//
//  def setCardOfDayDescription(context: TelegramContext, cardOfDayMode: CardOfDayMode, cardId: UUID, title: String, description: String)(
//    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
//    for {
//      _ <- ZIO.logInfo(s"Handle card of day description from chat ${context.chatId}")
//
//      _ <- sessionService.setPending(context.chatId, BotPending.CardOfDayPhoto(cardOfDayMode, cardId, title, description))
//      _ <- telegramApi.sendReplyText(context.chatId, s"Прикрепи фото для карты дня")
//    } yield ()
//
//  def setCardOfDayPhoto(context: TelegramContext, cardOfDayMode: CardOfDayMode, cardId: UUID, title: String, description: String, fileId: String)(
//    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
//    for {
//      _ <- ZIO.logInfo(s"Handle card of day photo from chat ${context.chatId}")
//
//      spread <- SessionRequire.spread(context.chatId)
//      token <- SessionRequire.token(context.chatId)
//
//      photo = PhotoRequest(FileSourceType.Telegram, fileId)
//      _ <- cardOfDayMode match {
//        case CardOfDayMode.Create =>
//          val request = CardOfDayCreateRequest(cardId, description, title, photo)
//          for {
//            cardOfDayId <- tarotApi.createCardOfDay(request, spread.spreadId, token)
//            _ <- sessionService.setCardOfDay(context.chatId, cardOfDayId.id)
//            _ <- telegramApi.sendText(context.chatId, s"Карта дня создана")
//          } yield cardId
//        case CardOfDayMode.Edit(cardOfDayId) =>
//          val request = CardOfDayUpdateRequest(cardId, description, title, photo)
//          for {
//            _ <- tarotApi.updateCardOfDay(request, cardOfDayId, token)
//            _ <- telegramApi.sendText(context.chatId, s"Карта дня обновлёна")
//          } yield cardId
//      }
//
//      _ <- selectCardOfDay(context, spread.spreadId)(telegramApi, tarotApi, sessionService)
//    } yield ()

//  private def startCardOfDayPending(context: TelegramContext, cardOfDayMode: CardOfDayMode)(
//    telegramApi: TelegramApiService, sessionService: BotSessionService) =
//    for {
//      _ <- sessionService.clearCardOfDay(context.chatId)
//      _ <- sessionService.setPending(context.chatId, BotPending.CardOfDayCardId(cardOfDayMode))
//      _ <- telegramApi.sendReplyText(context.chatId, s"Укажи номер карты дня для твоего расклада")
//    } yield ()

  private def startCardOfDayPending(context: TelegramContext, mode: CardOfDayMode)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] = {
    val pending = CardOfDayPending(mode, CardOfDayDraft.Start)
    setCardOfDayStartDraft(context, pending)(telegramApi, tarotApi, sessionService)
  }

  private def setCardOfDayStartDraft(context: TelegramContext, pending: CardOfDayPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardOfDayDraft.Start =>
        val nextDraft = CardOfDayDraft.AwaitingCardId
        val nextPending = CardOfDayPending(pending.mode, nextDraft)
        for {
          _ <- sessionService.setPending(context.chatId, BotPending.CardOfDay(nextPending))
          _ <- sendCardOfDayPendingReply(context, pending)(telegramApi, tarotApi, sessionService)
        } yield ()
      case _ =>
        ZIO.logError(s"Used card of day pending $pending instead of start draft chat=${context.chatId}") *>
          ZIO.fail(new IllegalStateException(s"Used card of day pending $pending instead of start draft"))
    }

  def setCardOfDayTextDraft(context: TelegramContext, text: String, pending: CardOfDayPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle text card of day draft ${pending.draft} from chat ${context.chatId}")

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
                    val nextAction = sendCardOfDayPendingReply(context, pending)(telegramApi, tarotApi, sessionService)
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
          val nextPending = sendCardOfDayPendingReply(context, pending)(telegramApi, tarotApi, sessionService)
          ZIO.succeed(nextDraft -> nextPending)
        case CardOfDayDraft.AwaitingDescription(cardId, title) =>
          val nextDraft = CardOfDayDraft.AwaitingPhoto(cardId, title, text)
          val nextPending = sendCardOfDayPendingReply(context, pending)(telegramApi, tarotApi, sessionService)
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

  def setCardOfDayPhotoDraft(context: TelegramContext, photoSourceId: String, pending: CardOfDayPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardOfDayDraft.AwaitingPhoto(cardId, title, description) =>
        val nextDraft = CardOfDayDraft.Complete(cardId, title, description, photoSourceId)
        val nextPending = CardOfDayPending(pending.mode, nextDraft)
        setCardOfDayCompleteDraft(context, nextPending)(telegramApi, tarotApi, sessionService)
      case _ =>
        ZIO.logInfo(s"Used photo instead of card of day text in chat ${context.chatId}") *>
          telegramApi.sendText(context.chatId, "Принимаю только текст!").unit
    }

  private def setCardOfDayCompleteDraft(context: TelegramContext, pending: CardOfDayPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardOfDayDraft.Complete(cardId, title, description, photoSourceId) =>
        val snapshot = CardOfDaySnapshot(cardId, title, description, photoSourceId)
        submitCardOfDay(context, pending.mode, snapshot)(telegramApi, tarotApi, sessionService)
      case _ =>
        ZIO.logError(s"Used card of day pending $pending instead of complete draft chat=${context.chatId}") *>
          ZIO.fail(new IllegalStateException(s"Used card of day pending $pending instead of complete draft"))
    }

  private def submitCardOfDay(context: TelegramContext, mode: CardOfDayMode, snapshot: CardOfDaySnapshot)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Submit card of day $mode from chat ${context.chatId}")

      token <- SessionRequire.token(context.chatId)
      spread <- SessionRequire.spread(context.chatId)
      _ <- mode match {
        case CardOfDayMode.Create =>
          for {
            cardOfDayId <- tarotApi.createCardOfDay(CardOfDaySnapshot.toCreateRequest(snapshot), spread.spreadId, token)
            _ <- sessionService.setCardOfDay(context.chatId, cardOfDayId.id)
            _ <- telegramApi.sendText(context.chatId, s"Карта дня создана")
          } yield cardOfDayId
        case CardOfDayMode.Edit(cardOfDayId) =>
          for {
            _ <- tarotApi.updateCardOfDay(CardOfDaySnapshot.toUpdateRequest(snapshot), cardOfDayId, token)
            _ <- telegramApi.sendText(context.chatId, s"Карта дня обновлёна")
          } yield cardOfDayId
      }
      _ <- selectCardOfDay(context, spread.spreadId)(telegramApi, tarotApi, sessionService)
    } yield ()

  private def sendCardOfDayPendingReply(context: TelegramContext, pending: CardOfDayPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      (buttonText, currentValue) <- getCardOfDayPendingReply(context, pending)(sessionService)
      _ <- pending.mode match {
        case CardOfDayMode.Create =>
          telegramApi.sendReplyText(context.chatId, buttonText)
        case CardOfDayMode.Edit(_) =>
          CommonFlow.sendEditReply(context, buttonText, currentValue)(telegramApi, tarotApi, sessionService)
      }
    } yield ()

  private def getCardOfDayPendingReply(context: TelegramContext, pending: CardOfDayPending)(sessionService: BotSessionService) =
    for {
      session <- sessionService.get(context.chatId)
      (buttonText, currentValue) <- pending.draft match {
        case CardOfDayDraft.Start =>
          ZIO.succeed("Укажи номер карты дня для твоего расклада" -> session.card.map(_.snapShot.title))
        case CardOfDayDraft.AwaitingCardId =>
          ZIO.succeed("Укажи название карты дня" -> session.spread.map(_.snapShot.cardsCount.toString))
        case CardOfDayDraft.AwaitingTitle(_) =>
          ZIO.succeed("Укажи подробное описание карты" -> session.spread.map(_.snapShot.cardsCount.toString))
        case CardOfDayDraft.AwaitingDescription(_,_) =>
          ZIO.succeed("Прикрепи фото для карты" -> session.spread.map(_.snapShot.description))
        case CardOfDayDraft.AwaitingPhoto(_, _, _) =>
          ZIO.logError(s"Card of day called for AwaitingPhoto state in chat ${context.chatId}") *>
            ZIO.dieMessage("Card of day called for AwaitingPhoto state")
        case CardOfDayDraft.Complete(_, _, _, _) =>
          ZIO.logError(s"Card of day called for Complete state in chat ${context.chatId}") *>
            ZIO.dieMessage("Card of day called for Complete state")
      }
    } yield (buttonText, currentValue)

  private def showCardOfDay(context: TelegramContext, cardOfDay: CardOfDayResponse, spread: BotSpread)(
    telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    val modifyButtons =
      if (SpreadStatus.isModify(spread.status))
        val editButton = TelegramInlineKeyboardButton("Изменить", Some(AuthorCommands.cardOfDayEdit(cardOfDay.id)))
        val deleteButton = TelegramInlineKeyboardButton("Удалить", Some(AuthorCommands.cardOfDayDelete(cardOfDay.id)))
        List(editButton, deleteButton)
      else Nil
    val backButton = TelegramInlineKeyboardButton("⬅ К раскладу", Some(AuthorCommands.spreadSelect(spread.spreadId)))    
    val photoButton = TelegramInlineKeyboardButton(s"🖼 Посмотреть фото", Some(TelegramCommands.showPhoto(cardOfDay.photo.id)))
    val buttons =  modifyButtons ++ List(photoButton, backButton)

    for {
      positionText <- getCardOfDayPositionText(context, Some(cardOfDay))(sessionService)
      summaryText =
        s""" Карта дня: “${cardOfDay.title}”
           | Номер карты: $positionText
           | Публикация: ${getScheduledText(cardOfDay)}
           |""".stripMargin        
    
      _ <- telegramApi.sendInlineButtons(context.chatId, summaryText, buttons)
    } yield ()

  def getCardOfDayPositionText(context: TelegramContext, cardOfDay: Option[CardOfDayResponse])
    (sessionService: BotSessionService): ZIO[BotEnv, Throwable, String] =
    ZIO.foreach(cardOfDay) { cardOfDay =>
      for {
        progress <- SessionRequire.spreadProgress(context.chatId)
      } yield
        progress.createdPositions.find(_.cardId == cardOfDay.cardId).map(_.position.+(1).toString).getOrElse("—")
    }.map(_.getOrElse("—"))

  private def getScheduledText(cardOfDay: CardOfDayResponse) =
    cardOfDay.scheduledAt match {
      case Some(scheduledAt) => DateFormatter.fromInstant(scheduledAt)
      case None => "—"
    }
}
