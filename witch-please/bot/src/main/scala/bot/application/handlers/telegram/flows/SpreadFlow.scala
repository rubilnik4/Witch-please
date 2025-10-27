package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.TelegramCommands
import bot.domain.models.session.{BotPendingAction, SpreadProgress}
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.spreads.*
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
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

  def selectSpread(context: TelegramContext, spreadId: UUID, cardCount: Int)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Select spread $spreadId from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)     
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))
      cards <- tarotApi.getCards(spreadId, token)
      
      createdIndexes = cards.map(_.index).toSet
      progress = SpreadProgress(cardCount, createdIndexes)     
      _ <- sessionService.setSpread(context.chatId, spreadId, progress)
      _ <- CardFlow.showCards(context, spreadId, cards)(telegramApi, tarotApi, sessionService)
    } yield ()
}
