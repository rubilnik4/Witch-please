package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.TelegramCommands
import bot.domain.models.session.BotPendingAction
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.cards.CardResponse
import shared.api.dto.tarot.spreads.TelegramCardCreateRequest
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
import zio.ZIO

import java.util.UUID

object CardFlow {
  def selectSpreadCards(context: TelegramContext, spreadId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Select spread cards $spreadId from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))
      cards <- tarotApi.getCards(spreadId, token)

      _ <- CardFlow.showCards(context, spreadId, cards)(telegramApi, tarotApi, sessionService)
    } yield ()
    
  private def showCards(context: TelegramContext, spreadId: UUID, cards: List[CardResponse])(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Get cards command by spread $spreadId for chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
      cardsCount <- ZIO.fromOption(session.spreadProgress.map(_.cardsCount))
        .orElseFail(new RuntimeException(s"Cards count not found in session for chat ${context.chatId}"))

      cardButtons = (1 to cardsCount).map { index =>
        cards.find(_.index == index - 1) match {
          case Some(card) =>
            TelegramInlineKeyboardButton(s"$index. ${card.description}", Some(TelegramCommands.authorCardCreate(index)))
          case None =>
            TelegramInlineKeyboardButton(s"$index. ➕ Создать карту", Some(TelegramCommands.authorCardCreate(index)))
        }
      }.toList
      backToSpreadButton = TelegramInlineKeyboardButton(s"К раскладу", Some(TelegramCommands.authorSpreadSelect(spreadId, cardsCount)))
      buttons = cardButtons :+ backToSpreadButton
      _ <- telegramApi.sendInlineButtons(context.chatId, "Выбери карту или создай новую", buttons)
    } yield ()

  def createCard(context: TelegramContext, index: Int)(
    telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Create card $index for chat ${context.chatId}")

      _ <- sessionService.setPending(context.chatId, BotPendingAction.CardDescription(index))
      _ <- telegramApi.sendReplyText(context.chatId, s"Напиши описание карты")
    } yield ()

  def setCardDescription(context: TelegramContext, index: Int, description: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle card description from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.CardPhoto(index, description))
      _ <- telegramApi.sendReplyText(context.chatId, s"Прикрепи фото для создания карты")
    } yield ()

  def setCardPhoto(context: TelegramContext, index: Int, description: String, fileId: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle card photo from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
      spreadId <- ZIO.fromOption(session.spreadId)
        .orElseFail(new RuntimeException(s"SpreadId not found in session for chat ${context.chatId}"))
      cardCount <- ZIO.fromOption(session.spreadProgress.map(_.cardsCount))
        .orElseFail(new RuntimeException(s"CardCount not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      request = TelegramCardCreateRequest(description, fileId)
      _ <- tarotApi.createCard(request, spreadId, index, token)
      _ <- sessionService.setCard(context.chatId, index)
      _ <- telegramApi.sendText(context.chatId, s"Карта $description создана")

      cards <- tarotApi.getCards(spreadId, token)
      _ <- showCards(context, spreadId, cards)(telegramApi, tarotApi, sessionService)
    } yield ()
}
