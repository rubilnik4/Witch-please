package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.AuthorCommands
import bot.domain.models.session.BotPendingAction
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.cards.CardResponse
import shared.api.dto.tarot.photo.PhotoRequest
import shared.api.dto.tarot.spreads.CardCreateRequest
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.files.FileSourceType
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

      cardButtons = (1 to cardsCount).map { position =>
        cards.find(_.position == position - 1) match {
          case Some(card) =>
            TelegramInlineKeyboardButton(s"$position. ${card.description}", Some(AuthorCommands.cardCreate(position)))
          case None =>
            TelegramInlineKeyboardButton(s"$position. ➕ Создать карту", Some(AuthorCommands.cardCreate(position)))
        }
      }.toList
      backToSpreadButton = TelegramInlineKeyboardButton(s"К раскладу", Some(AuthorCommands.spreadSelect(spreadId, cardsCount)))
      buttons = cardButtons :+ backToSpreadButton
      _ <- telegramApi.sendInlineButtons(context.chatId, "Выбери карту или создай новую", buttons)
    } yield ()

  def createCard(context: TelegramContext, position: Int)(
    telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Create card $position for chat ${context.chatId}")

      _ <- sessionService.setPending(context.chatId, BotPendingAction.CardTitle(position))
      _ <- telegramApi.sendReplyText(context.chatId, s"Напиши описание карты")
    } yield ()

  def setCardTitle(context: TelegramContext, position: Int, title: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle card title from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.CardPhoto(position, title))
      _ <- telegramApi.sendReplyText(context.chatId, s"Прикрепи фото для создания карты")
    } yield ()

  def setCardPhoto(context: TelegramContext, position: Int, title: String, fileId: String)(
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
      
      photo = PhotoRequest(FileSourceType.Telegram, fileId)
      request = CardCreateRequest(title, photo)
      _ <- tarotApi.createCard(request, spreadId, position, token)
      _ <- sessionService.setCard(context.chatId, position)
      _ <- telegramApi.sendText(context.chatId, s"Карта $title создана")

      cards <- tarotApi.getCards(spreadId, token)
      _ <- showCards(context, spreadId, cards)(telegramApi, tarotApi, sessionService)
    } yield ()
}
