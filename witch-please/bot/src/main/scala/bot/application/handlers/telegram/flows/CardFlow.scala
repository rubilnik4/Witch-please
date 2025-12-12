package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.AuthorCommands
import bot.application.handlers.telegram.flows.SpreadFlow.validateModifySpread
import bot.domain.models.session.{BotPendingAction, CardMode}
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.cards.*
import shared.api.dto.tarot.photo.PhotoRequest
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

      _ <- sessionService.clearCard(context.chatId)
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
            TelegramInlineKeyboardButton(s"$position. ${card.title}", Some(AuthorCommands.cardSelect(card.id)))
          case None =>
            TelegramInlineKeyboardButton(s"$position. ➕ Создать карту", Some(AuthorCommands.cardCreate(position)))
        }
      }.toList
      backButton = TelegramInlineKeyboardButton(s"К раскладу", Some(AuthorCommands.spreadSelect(spreadId, cardsCount)))
      buttons = cardButtons :+ backButton
      _ <- telegramApi.sendInlineButtons(context.chatId, "Выбери карту или создай новую", buttons)
    } yield ()

  def selectCard(context: TelegramContext, cardId: UUID)
                (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Get card settings command by cardId $cardId for chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
      spreadId <- ZIO.fromOption(session.spreadId)
        .orElseFail(new RuntimeException(s"SpreadId not found for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      card <- tarotApi.getCard(cardId, token)
      _ <- sessionService.setCard(context.chatId, cardId)

      _ <- showCard(context, card, spreadId)(telegramApi)
    } yield ()
    
  def createCard(context: TelegramContext, position: Int)(
    telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Create card $position for chat ${context.chatId}")

      _ <- startCardPending(context, CardMode.Create(position))(telegramApi, sessionService)
    } yield ()

  def editCard(context: TelegramContext, cardId: UUID)(
    telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Edit card $cardId for chat ${context.chatId}")

      _ <- startCardPending(context, CardMode.Edit(cardId))(telegramApi, sessionService)
    } yield ()

  def setCardTitle(context: TelegramContext, cardMode: CardMode, title: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle card title from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.CardDescription(cardMode, title))
      _ <- telegramApi.sendReplyText(context.chatId, s"Укажи подробное описание карты")
    } yield ()

  def setCardDescription(context: TelegramContext, cardMode: CardMode, title: String, description: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle card description from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)

      _ <- sessionService.setPending(context.chatId, BotPendingAction.CardPhoto(cardMode, title, description))
      _ <- telegramApi.sendReplyText(context.chatId, s"Прикрепи фото для карты")
    } yield ()

  def setCardPhoto(context: TelegramContext, cardMode: CardMode, title: String, description: String, fileId: String)(
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
      _ <- cardMode match {
        case CardMode.Create(position) =>
          val request = CardCreateRequest(position, title, description, photo)
          for {
            cardId <- tarotApi.createCard(request, spreadId, position, token)
            _ <- sessionService.setCardPositions(context.chatId, position)
            _ <- telegramApi.sendText(context.chatId, s"Карта создана")
          } yield cardId
        case CardMode.Edit(cardId) =>
          val request = CardUpdateRequest(title, description, photo)
          for {
            _ <- tarotApi.updateCard(request, cardId, token)
            _ <- telegramApi.sendText(context.chatId, s"Карта обновлёна")
          } yield cardId
      }

      _ <- selectSpreadCards(context, spreadId)(telegramApi, tarotApi, sessionService)
    } yield ()

  def deleteCard(context: TelegramContext, cardId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      session <- sessionService.get(context.chatId)
      spreadId <- ZIO.fromOption(session.spreadId)
        .orElseFail(new RuntimeException(s"SpreadId not found for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      _ <- ZIO.logInfo(s"Delete card $cardId for chat ${context.chatId}")
      _ <- validateModifySpread(context, spreadId, token)(telegramApi, tarotApi)

      _ <- tarotApi.deleteCard(cardId, token)
      _ <- telegramApi.sendText(context.chatId, s"Карта удалена")
      _ <- sessionService.clearCard(context.chatId)

      _ <- selectSpreadCards(context, spreadId)(telegramApi, tarotApi, sessionService)
    } yield ()

  private def startCardPending(context: TelegramContext, cardMode: CardMode)(
    telegramApi: TelegramApiService, sessionService: BotSessionService) =
    for {
      _ <- sessionService.clearCard(context.chatId)
      _ <- sessionService.setPending(context.chatId, BotPendingAction.CardTitle(cardMode))
      _ <- telegramApi.sendReplyText(context.chatId, s"Напиши название карты")
    } yield ()

  private def showCard(context: TelegramContext, card: CardResponse, spreadId: UUID)(telegramApi: TelegramApiService): ZIO[BotEnv, Throwable, Unit] =
    val summaryText =
      s""" Карта: “${card.title}”
         |
         |Выбери действие:
         |""".stripMargin
   
    val editButton = TelegramInlineKeyboardButton("Изменить", Some(AuthorCommands.cardEdit(card.id)))
    val deleteButton = TelegramInlineKeyboardButton("Удалить", Some(AuthorCommands.cardDelete(card.id)))
    val backButton = TelegramInlineKeyboardButton("⬅ К картам", Some(AuthorCommands.spreadCardsSelect(spreadId)))
    val buttons = List(editButton, deleteButton)

    for {
      _ <- telegramApi.sendInlineButtons(context.chatId, summaryText, buttons)
    } yield ()  
}
