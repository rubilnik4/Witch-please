package bot.application.handlers.telegram.flows

import bot.application.commands.ScheduleCommand
import bot.application.handlers.telegram.markup.SchedulerMarkup
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.infrastructure.services.telegram.TelegramApiService
import zio.ZIO

import java.time.YearMonth

object SchedulerFlow {
  def handle(context: TelegramContext, command: ScheduleCommand)
    (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
      command match {
        case ScheduleCommand.SelectMonth(month) =>
          for {
            dateButtons <- SchedulerMarkup.monthKeyboard(month)
            _ <- telegramApi.sendInlineGroupButtons(context.chatId, "Укажи дату публикации расклада", dateButtons)
          } yield ()     
        case ScheduleCommand.SelectDate(date) =>
          for {
            dateButtons <- SchedulerMarkup.monthKeyboard()
            _ <- telegramApi.sendInlineGroupButtons(context.chatId, "Укажи дату публикации расклада", dateButtons)
          } yield ()
        case ScheduleCommand.SelectTime(time) =>
          for {
            dateButtons <- SchedulerMarkup.monthKeyboard()
            _ <- telegramApi.sendInlineGroupButtons(context.chatId, "Укажи дату публикации расклада", dateButtons)
          } yield ()
        case ScheduleCommand.Confirm =>
          for {
            dateButtons <- SchedulerMarkup.monthKeyboard()
            _ <- telegramApi.sendInlineGroupButtons(context.chatId, "Укажи дату публикации расклада", dateButtons)
          } yield ()
      }
}
