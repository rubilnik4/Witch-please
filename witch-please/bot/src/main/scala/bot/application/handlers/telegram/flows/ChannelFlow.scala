package bot.application.handlers.telegram.flows

import bot.application.commands.telegram.*
import bot.domain.models.session.pending.BotPending
import bot.domain.models.session.{BotChannel, ChannelMode}
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.SessionRequire
import bot.layers.BotEnv
import shared.api.dto.tarot.channels.*
import shared.api.dto.telegram.*
import shared.models.api.ApiError
import zio.ZIO

import java.util.UUID

object ChannelFlow {
  def selectChannel(context: TelegramContext): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Select channel by user ${context.username} from chat ${context.chatId}")

      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      token <- SessionRequire.token(context.chatId)

      channelMaybe <- tarotApi.getDefaultChannel(token)
      _ <- channelMaybe match {
        case None =>
          createChannel(context)
        case Some(channel) =>
          showAuthorChannel(context, channel)
      }
    } yield ()

  def createChannel(context: TelegramContext): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Create channel by user ${context.username} from chat ${context.chatId}")

      _ <- startChannelPending(context, ChannelMode.Create)
    } yield ()

  def editChannel(context: TelegramContext, userChannelId: UUID): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Edit user channel $userChannelId for chat ${context.chatId}")

      _ <- startChannelPending(context, ChannelMode.Edit(userChannelId))
    } yield ()
    
  def setChannel(context: TelegramContext, channelMode: ChannelMode, channelId: Long, name: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Handle channel $channelId from chat ${context.chatId}")

      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      token <- SessionRequire.token(context.chatId)

      _ <- channelMode match {
        case ChannelMode.Create =>
          val request = ChannelCreateRequest(channelId, name)
          for {
            userChannelId <- tarotApi.createChannel(request, token).validateChannel(context)
            _ <- sessionService.setChannel(context.chatId, BotChannel(userChannelId.id, channelId))
            _ <- telegramApi.sendText(context.chatId, s"Канал привязан")
          } yield userChannelId
        case ChannelMode.Edit(userChannelId) =>
          val request = ChannelUpdateRequest(channelId, name)
          for {
            _ <- tarotApi.updateChannel(request, userChannelId, token).validateChannel(context)
            _ <- sessionService.setChannel(context.chatId, BotChannel(userChannelId, channelId))
            _ <- telegramApi.sendText(context.chatId, s"Канал обновлен")
          } yield userChannelId
      }

      _ <- SpreadFlow.selectSpreads(context)
    } yield ()

  private def startChannelPending(context: TelegramContext, channelMode: ChannelMode) =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      
      _ <- sessionService.setPending(context.chatId, BotPending.ChannelChannelId(channelMode))
      _ <- telegramApi.sendText(context.chatId, s"Добавь бот в свой канал и перешли ему любой пост из этого канала")
    } yield ()

  private def showAuthorChannel(context: TelegramContext, userChannel: UserChannelResponse) =
    for {
      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      _ <- sessionService.setChannel(context.chatId, BotChannel(userChannel.id, userChannel.channelId))
      _ <- showChannel(context, userChannel)
    } yield ()

  private def showChannel(context: TelegramContext, userChannel: UserChannelResponse): ZIO[BotEnv, Throwable, Unit] =
    val spreadsButton = TelegramInlineKeyboardButton("➡ К раскладам", Some(AuthorCommands.SpreadsSelect))
    val editButton = TelegramInlineKeyboardButton("Изменить", Some(AuthorCommands.channelEdit(userChannel.id)))
    val buttons = List(spreadsButton, editButton)

    val summaryText =
      s""" Канал
         | Название: ${userChannel.name}
         |""".stripMargin

    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      _ <- telegramApi.sendInlineButtons(context.chatId, summaryText, buttons)
    } yield ()

  extension [A](zio: ZIO[BotEnv, ApiError, A])
    private def validateChannel(context: TelegramContext): ZIO[BotEnv, ApiError, A] =
      zio.tapError {
        case ApiError.HttpCode(400, _) =>
          ZIO.serviceWithZIO[BotEnv](_.services.telegramApiService.sendText(
            context.chatId,
            "❗ Не удалось привязать канал.\n" + "Проверьте, что бот добавлен и является администратором."
          ))
        case _ => ZIO.unit
      }
}
