package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.AuthorCommands
import bot.domain.models.session.*
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.datetime.DateFormatter
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.photo.PhotoRequest
import shared.api.dto.tarot.spreads.*
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.files.FileSourceType
import zio.ZIO

import java.util.UUID

object SpreadFlow {  
  def showSpreads(context: TelegramContext, spreads: List[SpreadResponse])(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Get spreads command for chat ${context.chatId}")

      spreadButtons = spreads
        .sortBy(_.scheduledAt.fold(Long.MaxValue)(_.toEpochMilli))
        .zipWithIndex
        .map { case (spread, index) =>
          val label = s"${index + 1}. ${spread.title} (${getScheduledText(spread)})"
          val command = AuthorCommands.spreadSelect(spread.id, spread.cardsCount)
          TelegramInlineKeyboardButton(label, Some(command))
        }
      createButton = TelegramInlineKeyboardButton("➕ Создать новый", Some(AuthorCommands.SpreadCreate))
      buttons = spreadButtons :+ createButton
      _ <- telegramApi.sendInlineButtons(context.chatId, "Выбери расклад или создай новый", buttons)
    } yield ()

  def createSpread(context: TelegramContext)(
    telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Create spread for chat ${context.chatId}")

      _ <- startSpreadPending(context, SpreadMode.Create)(telegramApi, sessionService)
    } yield ()

  def editSpread(context: TelegramContext, spreadId: UUID)(
    telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Edit spread $spreadId for chat ${context.chatId}")

      _ <- startSpreadPending(context, SpreadMode.Edit(spreadId))(telegramApi, sessionService)
    } yield ()

  def setSpreadTitle(context: TelegramContext, spreadMode: SpreadMode, title: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle spread title from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.SpreadCardCount(spreadMode, title))
      _ <- telegramApi.sendReplyText(context.chatId, s"Укажи количество карт в раскладе")
    } yield ()

  def setSpreadCardCount(context: TelegramContext, spreadMode: SpreadMode, title: String, cardCount: Int)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle spread card count from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.SpreadPhoto(spreadMode, title, cardCount))
      _ <- telegramApi.sendReplyText(context.chatId, s"Прикрепи фото для создания расклада")
    } yield ()

  def setSpreadPhoto(context: TelegramContext, spreadMode: SpreadMode, title: String, cardCount: Int, fileId: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle spread photo from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
        .orElseFail(new RuntimeException(s"ProjectId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      photo = PhotoRequest(FileSourceType.Telegram, fileId)
      request = SpreadCreateRequest(title, cardCount, photo)
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
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))
      
      _ <- ZIO.logInfo(s"Delete spread $spreadId for chat ${context.chatId}")
      
      spread <- tarotApi.getSpread(spreadId, token)
      _ <- ZIO.unless(spread.publishedAt.isDefined) {
        telegramApi.sendText(context.chatId, s"Нельзя удалить опубликованный расклад") *>
          ZIO.logError(s"Can't delete published spread $spreadId") *>
          ZIO.fail(new RuntimeException("Can't delete published spread $spreadId"))
      }
      
      _ <- sessionService.clearSpread(context.chatId)
      spreads <- tarotApi.getSpreads(token)
      _ <- showSpreads(context, spreads)(telegramApi, tarotApi, sessionService)
    } yield ()

  private def startSpreadPending(context: TelegramContext, spreadMode: SpreadMode)(
    telegramApi: TelegramApiService, sessionService: BotSessionService) =
  for {
    _ <- sessionService.clearSpread(context.chatId)
    _ <- sessionService.setPending(context.chatId, BotPendingAction.SpreadTitle(spreadMode))
    _ <- telegramApi.sendReplyText(context.chatId, "Напиши название расклада")
  } yield ()
    
  private def showSpread(context: TelegramContext, spread: SpreadResponse, createdCardIndexes: Set[Int])
      (telegramApi: TelegramApiService): ZIO[BotEnv, Throwable, Unit] =
    val summaryText =
      s""" Расклад: “${spread.title}”
         | Публикация: ${getScheduledText(spread)}
         | Карт по плану: ${spread.cardsCount}
         | Создано карт: ${createdCardIndexes.size}
         |
         |Выбери действие:
         |""".stripMargin

    val cardsButton = TelegramInlineKeyboardButton("Карты", Some(AuthorCommands.spreadCardsSelect(spread.id)))
    val publishButton = TelegramInlineKeyboardButton("Публикация", Some(AuthorCommands.spreadPublish(spread.id)))
    val editButton = TelegramInlineKeyboardButton("Изменить", Some(AuthorCommands.spreadEdit(spread.id)))
    val deleteButton = TelegramInlineKeyboardButton("Удалить", Some(AuthorCommands.spreadDelete(spread.id)))
    val buttons = List(cardsButton, publishButton, editButton, deleteButton)

    for {
      _ <- telegramApi.sendInlineButtons(context.chatId, summaryText, buttons)
    } yield ()

  private def getScheduledText(spread: SpreadResponse) =
    spread.scheduledAt match {
      case Some(scheduledAt) => DateFormatter.fromInstant(scheduledAt)
      case None => "—"
  }
}
