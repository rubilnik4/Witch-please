package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.{AuthorCommands, TelegramCommands}
import bot.domain.models.session.pending.{CardDraft, CardPending}
import bot.domain.models.session.{BotCard, BotSpread, CardMode, CardSnapshot}
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.SessionRequire
import bot.layers.BotEnv
import shared.api.dto.tarot.cards.*
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.models.tarot.cards.CardPosition
import shared.models.tarot.spreads.SpreadStatus
import zio.ZIO

import java.util.UUID

object CardFlow {
  def selectCards(context: TelegramContext, spreadId: UUID): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Select cards by spread $spreadId from chat ${context.chatId}")

      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      token <- SessionRequire.token(context.chatId)

      _ <- sessionService.clearCard(context.chatId)
      cards <- tarotApi.getCards(spreadId, token)
      _ <- showCards(context, spreadId, cards)
    } yield ()
    
  private def showCards(context: TelegramContext, spreadId: UUID, cards: List[CardResponse]) =
    for {
      _ <- ZIO.logInfo(s"Get cards command by spread $spreadId for chat ${context.chatId}")

      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      cardsCount <- SessionRequire.cardsCount(context.chatId)

      cardButtons = (1 to cardsCount).map { position =>
        cards.find(_.position == position - 1) match {
          case Some(card) =>
            TelegramInlineKeyboardButton(s"$position. ${card.title}", Some(AuthorCommands.cardSelect(card.id)))
          case None =>
            TelegramInlineKeyboardButton(s"$position. ➕ Создать карту", Some(AuthorCommands.cardCreate(position)))
        }
      }.toList
      backButton = TelegramInlineKeyboardButton(s"⬅ К раскладу", Some(AuthorCommands.spreadSelect(spreadId)))
      buttons = cardButtons :+ backButton
      _ <- telegramApi.sendInlineButtons(context.chatId, "Выбери карту или создай новую", buttons)
    } yield ()

  def selectCard(context: TelegramContext, cardId: UUID): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Get card settings command by cardId $cardId for chat ${context.chatId}")

      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      spread <- SessionRequire.spread(context.chatId)
      token <- SessionRequire.token(context.chatId)

      card <- tarotApi.getCard(cardId, token)
      snapShot = CardSnapshot.toSnapShot(card)
      _ <- sessionService.setCard(context.chatId, BotCard(card.id, snapShot))

      _ <- showCard(context, card, spread)
    } yield ()
    
  def createCard(context: TelegramContext, position: Int): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Create card $position for chat ${context.chatId}")

      pending = CardPending(CardMode.Create(position), CardDraft.Start)
      _ <- CardDraftFlow.setCardStartDraft(context, pending)
    } yield ()

  def editCard(context: TelegramContext, cardId: UUID): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Edit card $cardId for chat ${context.chatId}")
      
      pending = CardPending(CardMode.Edit(cardId), CardDraft.Start)
      _ <- CardDraftFlow.setCardStartDraft(context, pending)
    } yield ()

  def deleteCard(context: TelegramContext, cardId: UUID): ZIO[BotEnv, Throwable, Unit] =
    for {
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      spread <- SessionRequire.spread(context.chatId)
      token <- SessionRequire.token(context.chatId)

      _ <- ZIO.logInfo(s"Delete card $cardId for chat ${context.chatId}")

      _ <- tarotApi.deleteCard(cardId, token)
      _ <- telegramApi.sendText(context.chatId, s"Карта удалена")
      _ <- sessionService.clearCard(context.chatId)
      _ <- sessionService.deleteCardPosition(context.chatId, cardId)

      _ <- selectCards(context, spread.spreadId)
    } yield ()

  def submitCard(context: TelegramContext, mode: CardMode, snapshot: CardSnapshot): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Submit card $mode from chat ${context.chatId}")

      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      token <- SessionRequire.token(context.chatId)
      spread <- SessionRequire.spread(context.chatId)
      
      _ <- mode match {
        case CardMode.Create(position) =>
          for {
            cardId <- tarotApi.createCard(CardSnapshot.toCreateRequest(position, snapshot), spread.spreadId, token)
            _ <- sessionService.setCardPosition(context.chatId, CardPosition(position, cardId.id))
            _ <- telegramApi.sendText(context.chatId, s"Карта создана")
          } yield cardId
        case CardMode.Edit(cardId) =>
          for {
            _ <- tarotApi.updateCard(CardSnapshot.toUpdateRequest(snapshot), cardId, token)
            _ <- telegramApi.sendText(context.chatId, s"Карта обновлёна")
          } yield cardId
      }
      _ <- selectCards(context, spread.spreadId)
    } yield ()
    
  private def showCard(context: TelegramContext, card: CardResponse, spread: BotSpread) =
    val modifyButtons =
      if (SpreadStatus.isModify(spread.status))
        val editButton = TelegramInlineKeyboardButton("Изменить", Some(AuthorCommands.cardEdit(card.id)))
        val deleteButton = TelegramInlineKeyboardButton("Удалить", Some(AuthorCommands.cardDelete(card.id)))
        List(editButton, deleteButton)
      else Nil  
    val backButton = TelegramInlineKeyboardButton("⬅ К картам", Some(AuthorCommands.spreadCardsSelect(spread.spreadId)))
    val photoButton = TelegramInlineKeyboardButton(s"🖼 Посмотреть фото", Some(TelegramCommands.showPhoto(card.photo.id)))
    val buttons = modifyButtons ++ List(photoButton, backButton)

    val summaryText =
      s""" Карта: “${card.title}”
         | Номер карты: ${card.position + 1}
         |""".stripMargin
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      _ <- telegramApi.sendInlineButtons(context.chatId, summaryText, buttons)
    } yield ()  
}
