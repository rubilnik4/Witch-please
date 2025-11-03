package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.TelegramCommands
import bot.domain.models.session.{BotPendingAction, SpreadProgress}
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.datetime.DateFormatter
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.spreads.*
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
import tarot.domain.models.spreads.Spread
import zio.ZIO

import java.util.UUID

object SpreadFlow {  
  def showSpreads(context: TelegramContext, projectId: UUID, spreads: List[SpreadResponse])(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Get spreads command by project $projectId for chat ${context.chatId}")
      
      spreadButtons = spreads.zipWithIndex.map { case (spread, index) =>
        TelegramInlineKeyboardButton(s"${index + 1}. ${spread.title}", 
          Some(TelegramCommands.spreadSelectCommand(spread.id, spread.cardCount)))
      }
      createButton = TelegramInlineKeyboardButton("➕ Создать новый", Some(TelegramCommands.SpreadCreate))
      buttons = spreadButtons :+ createButton
      _ <- telegramApi.sendInlineButtons(context.chatId, "Выбери расклад или создай новый", buttons)
    } yield ()

  def createSpread(context: TelegramContext)(
    telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Create spread for chat ${context.chatId}")

      _ <- sessionService.setPending(context.chatId, BotPendingAction.SpreadTitle)
      _ <- telegramApi.sendReplyText(context.chatId, "Напиши название расклада")
    } yield ()
    
  def setSpreadTitle(context: TelegramContext, title: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle spread title from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.SpreadCardCount(title))
      _ <- telegramApi.sendReplyText(context.chatId, s"Укажи количество карт в раскладе")
    } yield ()

  def setSpreadCardCount(context: TelegramContext, title: String, cardCount: Int)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle spread card count from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.SpreadPhoto(title, cardCount))
      _ <- telegramApi.sendReplyText(context.chatId, s"Прикрепи фото для создания расклада")
    } yield ()

  def setSpreadPhoto(context: TelegramContext, title: String, cardCount: Int, fileId: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle spread photo from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
      projectId <- ZIO.fromOption(session.projectId)
        .orElseFail(new RuntimeException(s"ProjectId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      request = TelegramSpreadCreateRequest(projectId, title, cardCount, fileId)
      spreadId <- tarotApi.createSpread(request, token).map(_.id)     

      _ <- telegramApi.sendText(context.chatId, s"Расклад $title создан")        
      _ <- selectSpread(context, spreadId, cardCount)(telegramApi, tarotApi, sessionService)
    } yield ()

  def selectSpread(context: TelegramContext, spreadId: UUID, cardCount: Int)
        (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Get spread settings command by spreadId $spreadId for chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      spread <- tarotApi.getSpread(spreadId, token)
      cards <- tarotApi.getCards(spreadId, token)
      createdIndexes = cards.map(_.index).toSet
      progress = SpreadProgress(cardCount, createdIndexes)
      _ <- sessionService.setSpread(context.chatId, spreadId, progress)

      _ <- showSpread(context, spread, createdIndexes)(telegramApi)
    } yield ()

  def deleteSpread(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      session <- sessionService.get(context.chatId)
      spreadId <- ZIO.fromOption(session.spreadId)
        .orElseFail(new RuntimeException(s"SpreadId not found for chat ${context.chatId}"))

      _ <- ZIO.logInfo(s"Delete spread $spreadId for chat ${context.chatId}")

      _ <- ZIO.unless(session.spreadProgress.exists(p => p.createdIndices.size == p.cardsCount)) {
        telegramApi.sendText(context.chatId, s"Нельзя опубликовать: не все карты загружены") *>
          ZIO.logError("Can't publish. Not all cards uploaded") *>
          ZIO.fail(new RuntimeException("Can't publish. Not all cards uploaded"))
      }

      today <- DateTimeService.currentLocalDate()
      dateButtons <- SchedulerMarkup.monthKeyboard(YearMonth.of(today.getYear, today.getMonth))
      _ <- telegramApi.sendInlineGroupButtons(context.chatId, "Укажи дату публикации расклада", dateButtons)
    } yield ()
    
  private def showSpread(context: TelegramContext, spread: SpreadResponse, createdCardIndexes: Set[Int])
      (telegramApi: TelegramApiService): ZIO[BotEnv, Throwable, Unit] =
    val scheduledText = spread.scheduledAt match {
      case Some(scheduledAt) => DateFormatter.fromInstant(scheduledAt)
      case None => "—"
    }

    val summaryText =
      s""" Расклад: “${spread.title}”
         | Публикация: $scheduledText
         | Карт по плану: ${spread.cardCount}
         | Создано карт: ${createdCardIndexes.size}
         |
         |Выбери действие:
         |""".stripMargin

    val cardsButton = TelegramInlineKeyboardButton("Карты", Some(TelegramCommands.spreadCardsSelectCommand(spread.id)))
    val publishButton = TelegramInlineKeyboardButton("Публикация", Some(TelegramCommands.spreadPublishCommand(spread.id)))
    val deleteButton = TelegramInlineKeyboardButton("Удалить", Some(TelegramCommands.spreadDeleteCommand(spread.id)))
    val buttons = List(cardsButton, publishButton, deleteButton)

    for {
      _ <- telegramApi.sendInlineButtons(context.chatId, summaryText, buttons)
    } yield ()
}
