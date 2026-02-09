package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.{AuthorCommands, TelegramCommands}
import bot.domain.models.session.*
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.datetime.DateFormatter
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.infrastructure.services.telegram.TelegramPhotoResolver
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

      session <- sessionService.get(context.chatId)
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))
     
      cardOfDayMaybe <- tarotApi.getCardOfDayBySpread(spreadId, token)
      _ <- cardOfDayMaybe match {
        case None =>
          createCardOfDay(context)(telegramApi, sessionService)
        case Some(cardOfDay) =>
          showSpreadCardOfDay(context, cardOfDay, spreadId)(telegramApi, tarotApi, sessionService)
      }
    } yield ()

  private def showSpreadCardOfDay(context: TelegramContext, cardOfDay: CardOfDayResponse, spreadId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      session <- sessionService.get(context.chatId)
      spread <- ZIO.fromOption(session.spread)
        .orElseFail(new RuntimeException(s"SpreadId not found for chat ${context.chatId}"))
      _ <- sessionService.setCardOfDay(context.chatId, cardOfDay.id)
      _ <- showCardOfDay(context, cardOfDay, spread)(telegramApi, sessionService)
    } yield ()

  def createCardOfDay(context: TelegramContext)(
    telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Create card of day for chat ${context.chatId}")

      _ <- startCardOfDayPending(context, CardOfDayMode.Create)(telegramApi, sessionService)
    } yield ()

  def editCardOfDay(context: TelegramContext, cardOfDayId: UUID)(
    telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Edit card of day $cardOfDayId for chat ${context.chatId}")

      _ <- startCardOfDayPending(context, CardOfDayMode.Edit(cardOfDayId))(telegramApi, sessionService)
    } yield ()

  def setCardOfDayCardId(context: TelegramContext, cardOfDayMode: CardOfDayMode, position: Int)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      session <- sessionService.get(context.chatId)
      progress <- ZIO.fromOption(session.spreadProgress).orElseFail(new RuntimeException("Spread progress not found"))
      _ <-
        if (progress.createdPositions.exists(_.position == position))
          for {
            _ <- ZIO.logInfo(s"Handle card id to card of day from chat ${context.chatId}")

            cardId <- ZIO.fromOption(progress.createdPositions.find(_.position == position).map(_.cardId))
              .orElseFail(new RuntimeException("Card id in spread progress not found"))
            _ <- sessionService.setPending(context.chatId, BotPendingAction.CardOfDayTitle(cardOfDayMode, cardId))
            _ <- telegramApi.sendReplyText(context.chatId, s"Укажи название карты дня")
          } yield ()
        else {
          for {
            spread <- ZIO.fromOption(session.spread).orElseFail(new RuntimeException("Spread id not found"))
            _ <- telegramApi.sendText(context.chatId, "Карта с такой позицией ещё не создана")
            _ <- SpreadFlow.selectSpread(context, spread.spreadId)(telegramApi, tarotApi, sessionService)
          } yield ()
        }
    } yield ()

  def setCardOfDayTitle(context: TelegramContext, cardOfDayMode: CardOfDayMode, cardId: UUID, title: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle card of day title from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.CardOfDayDescription(cardOfDayMode, cardId, title))
      _ <- telegramApi.sendReplyText(context.chatId, s"Укажи подробное описание карты дня")
    } yield ()  

  def setCardOfDayDescription(context: TelegramContext, cardOfDayMode: CardOfDayMode, cardId: UUID, title: String, description: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle card of day description from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.CardOfDayPhoto(cardOfDayMode, cardId, title, description))
      _ <- telegramApi.sendReplyText(context.chatId, s"Прикрепи фото для карты дня")
    } yield ()

  def setCardOfDayPhoto(context: TelegramContext, cardOfDayMode: CardOfDayMode, cardId: UUID, title: String, description: String, fileId: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle card of day photo from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
      spread <- ZIO.fromOption(session.spread)
        .orElseFail(new RuntimeException(s"SpreadId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      photo = PhotoRequest(FileSourceType.Telegram, fileId)
      _ <- cardOfDayMode match {
        case CardOfDayMode.Create =>
          val request = CardOfDayCreateRequest(cardId, description, title, photo)
          for {
            cardOfDayId <- tarotApi.createCardOfDay(request, spread.spreadId, token)
            _ <- sessionService.setCardOfDay(context.chatId, cardOfDayId.id)
            _ <- telegramApi.sendText(context.chatId, s"Карта дня создана")
          } yield cardId
        case CardOfDayMode.Edit(cardOfDayId) =>
          val request = CardOfDayUpdateRequest(cardId, description, title, photo)
          for {
            _ <- tarotApi.updateCardOfDay(request, cardOfDayId, token)
            _ <- telegramApi.sendText(context.chatId, s"Карта дня обновлёна")
          } yield cardId
      }

      _ <- selectCardOfDay(context, spread.spreadId)(telegramApi, tarotApi, sessionService)
    } yield ()

  def deleteCardOfDay(context: TelegramContext, cardOfDayId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      session <- sessionService.get(context.chatId)
      spread <- ZIO.fromOption(session.spread)
        .orElseFail(new RuntimeException(s"SpreadId not found for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      _ <- ZIO.logInfo(s"Delete card of day $cardOfDayId for chat ${context.chatId}")

      _ <- tarotApi.deleteCardOfDay(cardOfDayId, token)
      _ <- telegramApi.sendText(context.chatId, s"Карта дня удалена")
      _ <- sessionService.clearCardOfDay(context.chatId)

      _ <- SpreadFlow.selectSpread(context, spread.spreadId)(telegramApi, tarotApi, sessionService)
    } yield ()

  private def startCardOfDayPending(context: TelegramContext, cardOfDayMode: CardOfDayMode)(
    telegramApi: TelegramApiService, sessionService: BotSessionService) =
    for {
      _ <- sessionService.clearCardOfDay(context.chatId)
      _ <- sessionService.setPending(context.chatId, BotPendingAction.CardOfDayCardId(cardOfDayMode))
      _ <- telegramApi.sendReplyText(context.chatId, s"Укажи номер карты дня для твоего расклада")
    } yield ()

  private def showCardOfDay(context: TelegramContext, cardOfDay: CardOfDayResponse, spread: BotSpread)
                           (telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
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
        session <- sessionService.get(context.chatId)
        progress <- ZIO.fromOption(session.spreadProgress).orElseFail(new RuntimeException("Spread progress not found"))
      } yield
        progress.createdPositions.find(_.cardId == cardOfDay.cardId).map(_.position.+(1).toString).getOrElse("—")
    }.map(_.getOrElse("—"))

  private def getScheduledText(cardOfDay: CardOfDayResponse) =
    cardOfDay.scheduledAt match {
      case Some(scheduledAt) => DateFormatter.fromInstant(scheduledAt)
      case None => "—"
    }
}
