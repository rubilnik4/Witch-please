package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.{AuthorCommands, TelegramCommands}
import bot.domain.models.session.*
import bot.domain.models.session.pending.{BotPending, CardOfDayDraft, CardOfDayPending}
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.datetime.DateFormatter
import bot.infrastructure.services.sessions.{BotSessionService, SessionRequire}
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.cardsOfDay.*
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.telegram.TelegramApiService
import shared.models.tarot.spreads.SpreadStatus
import zio.ZIO

import java.util.UUID

object CardOfDayFlow {
  def selectCardOfDay(context: TelegramContext, spreadId: UUID): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Select card of day by spread $spreadId from chat ${context.chatId}")

      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      token <- SessionRequire.token(context.chatId)
     
      cardOfDayMaybe <- tarotApi.getCardOfDayBySpread(spreadId, token)
      _ <- cardOfDayMaybe match {
        case None =>
          createCardOfDay(context)
        case Some(cardOfDay) =>
          showSpreadCardOfDay(context, cardOfDay, spreadId)
      }
    } yield ()

  private def showSpreadCardOfDay(context: TelegramContext, cardOfDay: CardOfDayResponse, spreadId: UUID) =
    for {
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      spread <- SessionRequire.spread(context.chatId)

      snapShot = CardOfDaySnapshot.toSnapShot(cardOfDay)
      _ <- sessionService.setCardOfDay(context.chatId, BotCardOfDay(cardOfDay.id, snapShot))
      _ <- showCardOfDay(context, cardOfDay, spread)
    } yield ()

  def createCardOfDay(context: TelegramContext): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Create card of day for chat ${context.chatId}")

      pending = CardOfDayPending(CardOfDayMode.Create, CardOfDayDraft.Start)
      _ <- CardOfDayDraftFlow.setCardOfDayStartDraft(context, pending)
    } yield ()

  def editCardOfDay(context: TelegramContext, cardOfDayId: UUID): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Edit card of day $cardOfDayId for chat ${context.chatId}")

      pending = CardOfDayPending(CardOfDayMode.Edit(cardOfDayId), CardOfDayDraft.Start)
      _ <- CardOfDayDraftFlow.setCardOfDayStartDraft(context, pending)
    } yield ()

  def deleteCardOfDay(context: TelegramContext, cardOfDayId: UUID): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Delete card of day $cardOfDayId for chat ${context.chatId}")

      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      spread <- SessionRequire.spread(context.chatId)
      token <- SessionRequire.token(context.chatId)

      _ <- tarotApi.deleteCardOfDay(cardOfDayId, token)
      _ <- telegramApi.sendText(context.chatId, s"Карта дня удалена")
      _ <- sessionService.clearCardOfDay(context.chatId)

      _ <- SpreadFlow.selectSpread(context, spread.spreadId)
    } yield ()

  def submitCardOfDay(context: TelegramContext, mode: CardOfDayMode, snapshot: CardOfDaySnapshot): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Submit card of day $mode from chat ${context.chatId}")

      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      token <- SessionRequire.token(context.chatId)
      spread <- SessionRequire.spread(context.chatId)
      
      _ <- mode match {
        case CardOfDayMode.Create =>
          for {
            cardOfDayId <- tarotApi.createCardOfDay(CardOfDaySnapshot.toCreateRequest(snapshot), spread.spreadId, token)
            _ <- telegramApi.sendText(context.chatId, s"Карта дня создана")
          } yield cardOfDayId
        case CardOfDayMode.Edit(cardOfDayId) =>
          for {
            _ <- tarotApi.updateCardOfDay(CardOfDaySnapshot.toUpdateRequest(snapshot), cardOfDayId, token)
            _ <- telegramApi.sendText(context.chatId, s"Карта дня обновлёна")
          } yield cardOfDayId
      }
      _ <- selectCardOfDay(context, spread.spreadId)
    } yield ()

  private def showCardOfDay(context: TelegramContext, cardOfDay: CardOfDayResponse, spread: BotSpread) =
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
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      
      positionText <- getCardOfDayPositionText(context, Some(cardOfDay))
      summaryText =
        s""" Карта дня: “${cardOfDay.title}”
           | Номер карты: $positionText
           | Публикация: ${getScheduledText(cardOfDay)}
           |""".stripMargin        
    
      _ <- telegramApi.sendInlineButtons(context.chatId, summaryText, buttons)
    } yield ()

  def getCardOfDayPositionText(context: TelegramContext, cardOfDay: Option[CardOfDayResponse]): ZIO[BotEnv, Throwable, String] =
    ZIO.foreach(cardOfDay) { cardOfDay =>
      for {
        progress <- SessionRequire.spreadProgress(context.chatId)
      } yield
        progress.createdPositions.find(_.cardId == cardOfDay.cardId).map(_.position.+(1).toString).getOrElse("—")
    }.map(_.getOrElse("—"))

  private def getScheduledText(cardOfDay: CardOfDayResponse) =
    cardOfDay.scheduledAt match {
      case Some(scheduledAt) => DateFormatter.fromInstant(scheduledAt)
      case None => "—"
    }
}
