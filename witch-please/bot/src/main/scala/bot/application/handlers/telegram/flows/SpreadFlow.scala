package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.*
import bot.domain.models.session.*
import bot.domain.models.session.pending.*
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.datetime.DateFormatter
import bot.infrastructure.services.sessions.{BotSessionService, SessionRequire}
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.cardsOfDay.CardOfDayResponse
import shared.api.dto.tarot.spreads.*
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.tarot.cards.CardPosition
import shared.models.tarot.spreads.SpreadStatus
import zio.ZIO

import java.util.UUID

object SpreadFlow {
  def selectSpreads(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Select spreads from chat ${context.chatId}")

      token <- SessionRequire.token(context.chatId)

      _ <- sessionService.clearSpread(context.chatId)
      spreads <- tarotApi.getSpreads(token)
      _ <- showSpreads(context, spreads)(telegramApi, tarotApi, sessionService)
    } yield ()

  private def showSpreads(context: TelegramContext, spreads: List[SpreadResponse])(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Get spreads command for chat ${context.chatId}")

      spreadButtons = spreads
        .sortBy(_.scheduledAt.fold(Long.MaxValue)(_.toEpochMilli))
        .map { spread =>
          val label = s"${getPublishStatusImage(spread)} ${spread.title} - ${getScheduledText(spread)}"
          val command = AuthorCommands.spreadSelect(spread.id)
          TelegramInlineKeyboardButton(label, Some(command))
        }
      createButton = TelegramInlineKeyboardButton("➕ Создать новый", Some(AuthorCommands.SpreadCreate))
      buttons = spreadButtons :+ createButton
      _ <- telegramApi.sendInlineButtons(context.chatId, "Выбери расклад или создай новый", buttons)
    } yield ()

  def createSpread(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Create spread for chat ${context.chatId}")

      _ <- startSpreadPending(context, SpreadMode.Create)(telegramApi, tarotApi, sessionService)
    } yield ()

  def editSpread(context: TelegramContext, spreadId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Edit spread $spreadId for chat ${context.chatId}")

      _ <- startSpreadPending(context, SpreadMode.Edit(spreadId))(telegramApi, tarotApi, sessionService)
    } yield ()

  def cloneSpread(context: TelegramContext, spreadId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Clone spread $spreadId for chat ${context.chatId}")

      token <- SessionRequire.token(context.chatId)
      
      _ <- tarotApi.cloneSpread(spreadId, token).map(_.id)
      _ <- telegramApi.sendText(context.chatId, s"Расклад скопирован")
      
      _ <- selectSpreads(context)(telegramApi, tarotApi, sessionService)
    } yield ()

  def selectSpread(context: TelegramContext, spreadId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Get spread settings command by spreadId $spreadId for chat ${context.chatId}")

      token <- SessionRequire.token(context.chatId)

      spread <- tarotApi.getSpread(spreadId, token)
      cards <- tarotApi.getCards(spreadId, token)
      cardOfDay <- tarotApi.getCardOfDayBySpread(spreadId, token)
      createdPosition = cards.map(card => CardPosition(card.position, card.id)).toSet
      progress = SpreadProgress(spread.cardsCount, createdPosition)
      snapShot = SpreadSnapshot.toSnapShot(spread)
      _ <- sessionService.setSpread(context.chatId, BotSpread(spread.id, spread.status, snapShot), progress)

      _ <- showSpread(context, spread, createdPosition.size, cardOfDay)(telegramApi, sessionService)
    } yield ()

  def deleteSpread(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      token <- SessionRequire.token(context.chatId)
      spread <- SessionRequire.spread(context.chatId)

      _ <- ZIO.logInfo(s"Delete spread ${spread.spreadId} for chat ${context.chatId}")

      _ <- tarotApi.deleteSpread(spread.spreadId, token)
      _ <- telegramApi.sendText(context.chatId, s"Расклад удален")
      _ <- sessionService.clearSpread(context.chatId)

      _ <- selectSpreads(context)(telegramApi, tarotApi, sessionService)
    } yield ()

  def submitSpread(context: TelegramContext, mode: SpreadMode, snapshot: SpreadSnapshot)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Submit spread $mode from chat ${context.chatId}")

      token <- SessionRequire.token(context.chatId)
      spreadId <- mode match {
        case SpreadMode.Create =>
          for {
            spreadId <- tarotApi.createSpread(SpreadSnapshot.toCreateRequest(snapshot), token).map(_.id)
            _ <- telegramApi.sendText(context.chatId, s"Расклад создан")
          } yield spreadId
        case SpreadMode.Edit(spreadId) =>
          for {
            _ <- tarotApi.updateSpread(SpreadSnapshot.toUpdateRequest(snapshot), spreadId, token)
            _ <- telegramApi.sendText(context.chatId, s"Расклад обновлён")
          } yield spreadId
      }
      _ <- selectSpread(context, spreadId)(telegramApi, tarotApi, sessionService)
    } yield ()

  private def startSpreadPending(context: TelegramContext, mode: SpreadMode)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] = {
    val pending = SpreadPending(mode, SpreadDraft.Start)
    SpreadDraftFlow.setSpreadStartDraft(context, pending)(telegramApi, tarotApi, sessionService)
  }
  
  private def showSpread(context: TelegramContext, spread: SpreadResponse, cardsPositions: Int, cardOfDay: Option[CardOfDayResponse])(
    telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    val cardsButton = TelegramInlineKeyboardButton("Карты", Some(AuthorCommands.spreadCardsSelect(spread.id)))
    val cardOfDayButton = TelegramInlineKeyboardButton("Карта дня", Some(AuthorCommands.spreadCardOfDaySelect(spread.id)))
    val cloneButton = TelegramInlineKeyboardButton("Повторить", Some(AuthorCommands.spreadClone(spread.id)))
    val modifyButtons =
      if (SpreadStatus.isModify(spread.status))
        val publishButton = TelegramInlineKeyboardButton("Публикация", Some(AuthorCommands.spreadPublish(spread.id)))
        val editButton = TelegramInlineKeyboardButton("Изменить", Some(AuthorCommands.spreadEdit(spread.id)))
        val deleteButton = TelegramInlineKeyboardButton("Удалить", Some(AuthorCommands.spreadDelete(spread.id)))
        List(publishButton, editButton, deleteButton)
      else Nil
    val photoButton = TelegramInlineKeyboardButton(s"🖼 Посмотреть фото", Some(TelegramCommands.showPhoto(spread.photo.id))) 
    val backButton = TelegramInlineKeyboardButton("⬅ К раскладам", Some(AuthorCommands.SpreadsSelect))
    val buttons = List(cardsButton, cardOfDayButton) ++ modifyButtons ++ List(cloneButton, photoButton, backButton)

    for {
      cardOfDayText <- CardOfDayFlow.getCardOfDayPositionText(context, cardOfDay)(sessionService)
      summaryText =
        s""" Расклад: “${spread.title}”         
           | Карт по плану: ${spread.cardsCount}
           | Создано карт: $cardsPositions
           | Номер карты дня: $cardOfDayText
           | Статус: ${getPublishStatusImage(spread)} ${getPublishStatusText(spread)}
           | Публикация: ${getScheduledText(spread)}
           | Публикация карты дня: ${getCardOfDayScheduledText(cardOfDay)}
           |""".stripMargin
   
      _ <- telegramApi.sendInlineButtons(context.chatId, summaryText, buttons)
    } yield ()

  private def getPublishStatusImage(spread: SpreadResponse) =
    (spread.publishedAt, spread.scheduledAt) match {
      case (Some(publishedAt), _) => s"🟢"
      case (None, Some(scheduledAt)) => s"🕒"
      case (_,_) => s"⚪"
    }

  private def getPublishStatusText(spread: SpreadResponse) =
    (spread.publishedAt, spread.scheduledAt) match {
      case (Some(publishedAt), _) => "опубликован"
      case (None, Some(scheduledAt)) => "к публикации"
      case (_, _) => "черновик"
    }

  private def getScheduledText(spread: SpreadResponse) =
    spread.scheduledAt match {
      case Some(scheduledAt) => DateFormatter.fromInstant(scheduledAt)
      case None => "—"
  }

  private def getCardOfDayScheduledText(cardOfDay: Option[CardOfDayResponse]) =
    cardOfDay.flatMap(_.scheduledAt) match {
      case Some(scheduledAt) => DateFormatter.fromInstant(scheduledAt)
      case None => "—"
    }
}
