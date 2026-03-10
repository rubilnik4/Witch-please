package bot.application.handlers.telegram.flows

import bot.application.handlers.telegram.markup.SchedulerMarkup
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.{BotSessionService, SessionRequire}
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.infrastructure.services.common.DateTimeService
import shared.infrastructure.services.telegram.TelegramApiService
import zio.ZIO

import java.time.YearMonth

object PublishFlow {    
  def publishSpread(context: TelegramContext)(
    telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
    for {
      spread <- SessionRequire.spread(context.chatId)
      progress <- SessionRequire.spreadProgress(context.chatId)

      _ <- ZIO.logInfo(s"Publish spread ${spread.spreadId} for chat ${context.chatId}")

      _ <- ZIO.unless(progress.createdPositions.size == progress.cardsCount) {
        telegramApi.sendText(context.chatId, s"Нельзя опубликовать: не все карты загружены") *>
          ZIO.logError("Can't publish. Not all cards uploaded") *>
          ZIO.fail(new RuntimeException("Can't publish. Not all cards uploaded"))
      }
      
      today <- DateTimeService.currentLocalDate()
      dateButtons <- SchedulerMarkup.monthKeyboard(YearMonth.of(today.getYear, today.getMonth))
      _ <- telegramApi.sendInlineGroupButtons(context.chatId, "Укажи дату публикации расклада", dateButtons)
    } yield ()
}
