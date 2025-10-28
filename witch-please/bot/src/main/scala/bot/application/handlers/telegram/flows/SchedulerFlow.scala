package bot.application.handlers.telegram.flows

import bot.application.commands.ScheduleCommand
import bot.application.commands.telegram.SchedulerCommands
import bot.application.handlers.telegram.markup.SchedulerMarkup
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.calendar.CalendarService
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.spreads.SpreadPublishRequest
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.common.DateTimeService
import shared.infrastructure.services.telegram.TelegramApiService
import zio.ZIO

import java.time.{LocalDate, LocalDateTime, LocalTime, YearMonth}

object SchedulerFlow {
  def handle(context: TelegramContext, command: ScheduleCommand)
    (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
      command match {
        case ScheduleCommand.SelectMonth(month) =>
          for {
            _ <- ZIO.logInfo(s"Select month $month from chat ${context.chatId}")
            
            today <- DateTimeService.currentLocalDate()
            _ <- ZIO.unless(CalendarService.isPrevMonthEnable(today, month)) {
              ZIO.logError(s"Can't select month: $month before current ${today.getMonth}") *>
                ZIO.fail(new RuntimeException(s"Can't select month: $month before current ${today.getMonth}"))
            }

            dateButtons <- SchedulerMarkup.monthKeyboard(month)
            _ <- telegramApi.sendInlineGroupButtons(context.chatId, "Укажи дату публикации расклада", dateButtons)
          } yield ()     
        case ScheduleCommand.SelectDate(date) =>
          for {
            _ <- ZIO.logInfo(s"Select date $date from chat ${context.chatId}")

            today <- DateTimeService.currentLocalDate()
            _ <- ZIO.unless(CalendarService.isPrevDayEnable(today, date)) {
              ZIO.logError(s"Can't select date: $date before current $today") *>
                ZIO.fail(new RuntimeException(s"Can't select date: $date before current $today"))
            }
            
            _ <- sessionService.setDate(context.chatId, date)
            _ <- showTimeKeyboard(context, date, 0)(telegramApi)
          } yield ()
        case ScheduleCommand.SelectTimePage(page) =>
          for {
            _ <- ZIO.logInfo(s"Select time page $page from chat ${context.chatId}")

            session <- sessionService.get(context.chatId)
            date <- ZIO.fromOption(session.date)
              .orElseFail(new RuntimeException(s"Date not found in session for chat ${context.chatId}"))
            _ <- showTimeKeyboard(context, date, page)(telegramApi)
          } yield ()
        case ScheduleCommand.SelectTime(time) =>
          selectTime(context, time)(telegramApi, tarotApi, sessionService)
        case ScheduleCommand.Confirm(dateTime) =>
          confirmDateTime(context, dateTime)(telegramApi, tarotApi, sessionService)
      }

  private def selectTime(context: TelegramContext, time: LocalTime)
      (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      _ <- ZIO.logInfo(s"Select time $time from chat ${context.chatId}")

      _ <- sessionService.setTime(context.chatId, time)
      session <- sessionService.get(context.chatId)
      date <- ZIO.fromOption(session.date)
        .orElseFail(new RuntimeException(s"Date not found in session for chat ${context.chatId}"))
      dateTime = LocalDateTime.of(date, time)
      
      today <- DateTimeService.currentLocalDateTime()
      _ <- ZIO.unless(CalendarService.isPrevTimeEnable(today, dateTime)) {
        ZIO.logError(s"Can't select datetime: $dateTime before current $today") *>
          ZIO.fail(new RuntimeException(s"Can't select date: $date before current $today"))
      }      
      
      month = YearMonth.of(date.getYear, date.getMonth)
      buttons = List(
        TelegramInlineKeyboardButton("Назад к дате", Some(SchedulerCommands.selectMonth(month))),
        TelegramInlineKeyboardButton("Подтвердить", Some(SchedulerCommands.confirm(dateTime)))
      )
      text = s"""Время публикации: ${date.toString} f"${time.getHour}%02d:${time.getMinute}%02d". Подтвердить?"""
      _ <- telegramApi.sendInlineButtons(context.chatId, text, buttons)
    } yield ()

  private def confirmDateTime(context: TelegramContext, dateTime: LocalDateTime)
      (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      _ <- ZIO.logInfo(s"Confirm datetime $dateTime from chat ${context.chatId}")

      session <- sessionService.get(context.chatId)
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found for chat ${context.chatId}"))
      spreadId <- ZIO.fromOption(session.spreadId)
        .orElseFail(new RuntimeException(s"SpreadId not found for chat ${context.chatId}"))

      zoneOffset <- DateTimeService.getOffset
      scheduledAt = dateTime.toInstant(zoneOffset)
      request = SpreadPublishRequest(scheduledAt)
      _ <- tarotApi.publishSpread(request, spreadId, token)

      _ <- telegramApi.sendText(context.chatId, s"Расклад будет опубликован $dateTime")
      _ <- sessionService.reset(context.chatId)
    } yield ()

  private def showTimeKeyboard(context: TelegramContext, date: LocalDate, page: Int)
      (telegramApi: TelegramApiService): ZIO[Any, Throwable, Unit] =
    for {
      dateButtons <- SchedulerMarkup.timeKeyboard(date, page)
      _ <- telegramApi.sendInlineGroupButtons(context.chatId, "Укажи время публикации расклада", dateButtons)
    } yield ()
}
