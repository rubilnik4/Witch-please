package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.*
import bot.domain.models.session.{BotChannel, BotPendingAction, ChannelMode}
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.channels.*
import shared.api.dto.telegram.*
import shared.infrastructure.services.telegram.TelegramApiService
import zio.ZIO

import java.util.UUID

object ChannelFlow {
  def selectChannel(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Select channel by user ${context.username} from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      channelMaybe <- tarotApi.getDefaultChannel(token)
      _ <- channelMaybe match {
        case None =>
          createChannel(context)(telegramApi, tarotApi, sessionService)
        case Some(channel) =>
          showAuthorChannel(context, channel)(telegramApi, tarotApi, sessionService)
      }
    } yield ()

  private def showAuthorChannel(context: TelegramContext, userChannel: UserChannelResponse)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      session <- sessionService.get(context.chatId)
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))
      _ <- sessionService.setChannel(context.chatId, BotChannel(userChannel.id, userChannel.channelId))
      _ <- showChannel(context, userChannel)(telegramApi, sessionService)
    } yield ()

  def createChannel(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Create channel by user ${context.username} from chat ${context.chatId}")

      _ <- startChannelPending(context, ChannelMode.Create)(telegramApi, sessionService)
    } yield ()

  def editChannel(context: TelegramContext, userChannelId: UUID)(
    telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Edit user channel $userChannelId for chat ${context.chatId}")

      _ <- startChannelPending(context, ChannelMode.Edit(userChannelId))(telegramApi, sessionService)
    } yield ()
    
  def setChannel(context: TelegramContext, channelMode: ChannelMode, channelId: Long, name: String)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle channel $channelId from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      _ <- channelMode match {
        case ChannelMode.Create =>
          val request = ChannelCreateRequest(channelId, name)
          for {
            userChannelId <- tarotApi.createChannel(request, token)
            _ <- sessionService.setChannel(context.chatId, BotChannel(userChannelId.id, channelId))
            _ <- telegramApi.sendText(context.chatId, s"Канал привязан")
          } yield userChannelId
        case ChannelMode.Edit(userChannelId) =>
          val request = ChannelUpdateRequest(channelId, name)
          for {
            _ <- tarotApi.updateChannel(request, userChannelId, token)
            _ <- sessionService.setChannel(context.chatId, BotChannel(userChannelId, channelId))
            _ <- telegramApi.sendText(context.chatId, s"Канал обновлен")
          } yield userChannelId
      }

      _ <- SpreadFlow.selectSpreads(context)(telegramApi, tarotApi, sessionService)
    } yield ()

  private def startChannelPending(context: TelegramContext, channelMode: ChannelMode)(
    telegramApi: TelegramApiService, sessionService: BotSessionService) =
    for {
      _ <- sessionService.clearChannel(context.chatId)
      _ <- sessionService.setPending(context.chatId, BotPendingAction.ChannelChannelId(channelMode))
      _ <- telegramApi.sendReplyText(context.chatId, s"Добавь бот в свой канал и отправь ему любое сообщение")
    } yield ()

  private def showChannel(context: TelegramContext, userChannel: UserChannelResponse)(
    telegramApi: TelegramApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    val editButton = TelegramInlineKeyboardButton("Изменить", Some(AuthorCommands.channelEdit(userChannel.id)))
    val buttons = List(editButton)

    val summaryText =
      s""" Канал”
         | Название: ${userChannel.name}
         | Идентификатор: ${userChannel.channelId}
         |
         |Выбери действие:
         |""".stripMargin

    for {
      _ <- telegramApi.sendInlineButtons(context.chatId, summaryText, buttons)
    } yield ()
}
