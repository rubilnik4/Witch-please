package bot.application.handlers.telegram

import bot.domain.models.session.BotSession
import bot.domain.models.telegram.*
import bot.layers.*
import shared.api.dto.tarot.spreads.*
import zio.*

object TelegramPendingHandler {
  def handleSpreadCover(context: TelegramContext, session: BotSession,
                        title: String, cardCount: Int, fileId: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramChannelService)
      botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
      tarotApiService <- ZIO.serviceWith[BotEnv](_.botService.tarotApiService)

      _ <- telegramApiService.sendText(context.chatId, s"Создаю расклад '$title'...")
      projectId <- ZIO.fromOption(session.projectId)
        .orElseFail(new RuntimeException(s"ProjectId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      _ <- ZIO.logDebug(s"Handle spread cover from chat ${context.chatId} for project $projectId")
      request = TelegramSpreadCreateRequest(projectId, title, cardCount, fileId)
      spreadId <- tarotApiService.createSpread(request, token).map(_.id)
      _ <- botSessionService.setSpread(context.chatId, spreadId, cardCount)
    } yield ()

  def handleCardCover(context: TelegramContext, session: BotSession,
                      description: String, index: Int, fileId: String): ZIO[BotEnv, Throwable, Unit] =
    for {
      telegramApiService <- ZIO.serviceWith[BotEnv](_.botService.telegramChannelService)
      botSessionService <- ZIO.serviceWith[BotEnv](_.botService.botSessionService)
      tarotApiService <- ZIO.serviceWith[BotEnv](_.botService.tarotApiService)

      _ <- telegramApiService.sendText(context.chatId, s"Создаю карту '$description'...")
      spreadId <- ZIO.fromOption(session.spreadId)
        .orElseFail(new RuntimeException(s"SpreadId not found in session for chat ${context.chatId}"))
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found in session for chat ${context.chatId}"))

      _ <- ZIO.logDebug(s"Handle card $index cover from chat ${context.chatId} for spread $spreadId")
      request = TelegramCardCreateRequest(description, fileId)
      _ <- tarotApiService.createCard(request, spreadId, index, token)
      _ <- botSessionService.setCard(context.chatId, index)
    } yield ()
}
