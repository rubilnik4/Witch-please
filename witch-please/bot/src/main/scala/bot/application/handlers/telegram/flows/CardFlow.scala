package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.{AuthorCommands, TelegramCommands}
import bot.domain.models.session.pending.{BotPending, CardDraft, CardPending}
import bot.domain.models.session.{BotCard, BotSpread, CardMode, CardSnapshot}
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.{BotSessionService, SessionRequire}
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.cards.*
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.tarot.cards.CardPosition
import shared.models.tarot.spreads.SpreadStatus
import zio.ZIO

import java.util.UUID

object CardFlow {
  def selectCards(context: TelegramContext, spreadId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Select cards by spread $spreadId from chat ${context.chatId}")

      token <- SessionRequire.token(context.chatId)

      _ <- sessionService.clearCard(context.chatId)
      cards <- tarotApi.getCards(spreadId, token)
      _ <- showCards(context, spreadId, cards)(telegramApi, tarotApi, sessionService)
    } yield ()
    
  private def showCards(context: TelegramContext, spreadId: UUID, cards: List[CardResponse])(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Get cards command by spread $spreadId for chat ${context.chatId}")

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

  def selectCard(context: TelegramContext, cardId: UUID)
                (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Get card settings command by cardId $cardId for chat ${context.chatId}")

      spread <- SessionRequire.spread(context.chatId)
      token <- SessionRequire.token(context.chatId)

      card <- tarotApi.getCard(cardId, token)
      snapShot = CardSnapshot.toSnapShot(card)
      _ <- sessionService.setCard(context.chatId, BotCard(card.id, snapShot))

      _ <- showCard(context, card, spread)(telegramApi)
    } yield ()
    
  def createCard(context: TelegramContext, position: Int)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Create card $position for chat ${context.chatId}")

      _ <- startCardPending(context, CardMode.Create(position))(telegramApi, tarotApi, sessionService)
    } yield ()

  def editCard(context: TelegramContext, cardId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Edit card $cardId for chat ${context.chatId}")

      _ <- startCardPending(context, CardMode.Edit(cardId))(telegramApi, tarotApi, sessionService)
    } yield ()

  def deleteCard(context: TelegramContext, cardId: UUID)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      spread <- SessionRequire.spread(context.chatId)
      token <- SessionRequire.token(context.chatId)

      _ <- ZIO.logInfo(s"Delete card $cardId for chat ${context.chatId}")

      _ <- tarotApi.deleteCard(cardId, token)
      _ <- telegramApi.sendText(context.chatId, s"Карта удалена")
      _ <- sessionService.clearCard(context.chatId)
      _ <- sessionService.deleteCardPosition(context.chatId, cardId)

      _ <- selectCards(context, spread.spreadId)(telegramApi, tarotApi, sessionService)
    } yield ()

  private def startCardPending(context: TelegramContext, cardMode: CardMode)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] = {
    val pending = CardPending(cardMode, CardDraft.Start)
    setCardStartDraft(context, pending)(telegramApi, tarotApi, sessionService)
  }
  
  private def setCardStartDraft(context: TelegramContext, pending: CardPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardDraft.Start =>
        val nextDraft = CardDraft.AwaitingTitle
        val nextPending = CardPending(pending.mode, nextDraft)
        for {
          _ <- sessionService.setPending(context.chatId, BotPending.Card(nextPending))
          _ <- sendCardPendingReply(context, pending)(telegramApi, tarotApi, sessionService)
        } yield ()
      case _ =>
        ZIO.logError(s"Used card pending $pending instead of start draft chat=${context.chatId}") *>
          ZIO.fail(new IllegalStateException(s"Used card pending $pending instead of start draft"))
    }

  def setCardTextDraft(context: TelegramContext, text: String, pending: CardPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle text card draft ${pending.draft} from chat ${context.chatId}")

      (nextDraft, nextAction) <- pending.draft match {
        case CardDraft.Start | CardDraft.Complete(_,_,_) =>
          ZIO.logError(s"Couldn't used card start or complete pending in text draft chat=${context.chatId}") *>
            ZIO.fail(new IllegalStateException(s"Couldn't used card start or complete  pending in text draft"))
        case CardDraft.AwaitingTitle =>
          val nextDraft = CardDraft.AwaitingDescription(text)
          val nextPending = sendCardPendingReply(context, pending)(telegramApi, tarotApi, sessionService)
          ZIO.succeed(nextDraft -> nextPending)       
        case CardDraft.AwaitingDescription(title) =>
          val nextDraft = CardDraft.AwaitingPhoto(title, text)
          val nextPending = sendCardPendingReply(context, pending)(telegramApi, tarotApi, sessionService)
          ZIO.succeed(nextDraft -> nextPending)
        case draft @ CardDraft.AwaitingPhoto(_,_) =>
          val nextPending =
            ZIO.logInfo(s"Used text instead of card photo chat=${context.chatId}") *>
              telegramApi.sendText(context.chatId, "Принимаю только фото!").unit
          ZIO.succeed(draft -> nextPending)
      }
      nextPending = CardPending(pending.mode, nextDraft)
      _ <- sessionService.setPending(context.chatId, BotPending.Card(nextPending))
      _ <- nextAction
    } yield ()

  def setCardPhotoDraft(context: TelegramContext, photoSourceId: String, pending: CardPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardDraft.AwaitingPhoto(title, description) =>
        val nextDraft = CardDraft.Complete(title, description, photoSourceId)
        val nextPending = CardPending(pending.mode, nextDraft)
        setCardCompleteDraft(context, nextPending)(telegramApi, tarotApi, sessionService)
      case _ =>
        ZIO.logInfo(s"Used photo instead of card text chat=${context.chatId}") *>
          telegramApi.sendText(context.chatId, "Принимаю только текст!").unit
    }

  private def setCardCompleteDraft(context: TelegramContext, pending: CardPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    pending.draft match {
      case CardDraft.Complete(title, description, photoSourceId) =>
        val snapshot = CardSnapshot(title, description, photoSourceId)
        submitCard(context, pending.mode, snapshot)(telegramApi, tarotApi, sessionService)
      case _ =>
        ZIO.logError(s"Used card pending $pending instead of complete draft chat=${context.chatId}") *>
          ZIO.fail(new IllegalStateException(s"Used card pending $pending instead of complete draft"))
    }
    
  private def submitCard(context: TelegramContext, cardMode: CardMode, snapshot: CardSnapshot)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Submit card $cardMode from chat ${context.chatId}")

      token <- SessionRequire.token(context.chatId)
      spread <- SessionRequire.spread(context.chatId)
      _ <- cardMode match {
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
      _ <- selectCards(context, spread.spreadId)(telegramApi, tarotApi, sessionService)
    } yield ()

  private def sendCardPendingReply(context: TelegramContext, pending: CardPending)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      (buttonText, currentValue) <- getCardPendingReply(context, pending)(sessionService)
      _ <- pending.mode match {
        case CardMode.Create(_) =>
          telegramApi.sendReplyText(context.chatId, buttonText)
        case CardMode.Edit(_) =>
          CommonFlow.sendEditReply(context, buttonText, currentValue)(telegramApi, tarotApi, sessionService)
      }
    } yield ()

  private def getCardPendingReply(context: TelegramContext, pending: CardPending)(sessionService: BotSessionService) =
    for {
      session <- sessionService.get(context.chatId)
      (buttonText, currentValue) <- pending.draft match {
        case CardDraft.Start =>
          ZIO.succeed("Напиши название карты" -> session.card.map(_.snapShot.title))
        case CardDraft.AwaitingTitle =>
          ZIO.succeed("Укажи подробное описание карты" -> session.spread.map(_.snapShot.cardsCount.toString))
        case CardDraft.AwaitingDescription(_) =>
          ZIO.succeed("Прикрепи фото для карты" -> session.spread.map(_.snapShot.description))
        case CardDraft.AwaitingPhoto(_,_) =>
          ZIO.logError(s"setCardParameter called for AwaitingPhoto state chat=${context.chatId}") *>
            ZIO.dieMessage("setCardParameter called for AwaitingPhoto state")
        case CardDraft.Complete(_,_,_) =>
          ZIO.logError(s"setCardParameter called for Complete state chat=${context.chatId}") *>
            ZIO.dieMessage("setCardParameter called for Complete state")
      }
    } yield (buttonText, currentValue)
    
  private def showCard(context: TelegramContext, card: CardResponse, spread: BotSpread)(telegramApi: TelegramApiService): ZIO[BotEnv, Throwable, Unit] =
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
      _ <- telegramApi.sendInlineButtons(context.chatId, summaryText, buttons)
    } yield ()  
}
