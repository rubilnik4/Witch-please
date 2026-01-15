package bot.application.handlers.telegram.flows

import bot.application.commands.ScheduleCommand
import bot.application.commands.telegram.SchedulerCommands
import bot.application.handlers.telegram.markup.SchedulerMarkup
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.calendar.CalendarService
import bot.infrastructure.services.datetime.DateFormatter
import bot.infrastructure.services.sessions.BotSessionService
import bot.infrastructure.services.tarot.TarotApiService
import bot.layers.BotEnv
import shared.api.dto.tarot.spreads.SpreadPublishRequest
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.common.DateTimeService
import shared.infrastructure.services.telegram.TelegramApiService
import zio.ZIO

import java.time.{Duration, LocalDate, LocalDateTime, LocalTime, YearMonth}

object SchedulerFlow {
  def handle(context: TelegramContext, command: ScheduleCommand)
    (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService): ZIO[BotEnv, Throwable, Unit] =
      command match {
        case ScheduleCommand.SelectMonth(month) =>
          for {
            _ <- ZIO.logInfo(s"Select month $month from chat ${context.chatId}")

            _ <- sessionService.clearDateTime(context.chatId)

            projectConfig <- ZIO.serviceWith[BotEnv](_.config.project)
            today <- DateTimeService.currentLocalDate()
            _ <- ZIO.unless(CalendarService.isPrevMonthEnable(today, month) &&
                            CalendarService.isNextMonthEnable(today, month, projectConfig.maxFutureTime)) {
              ZIO.logError(s"Can't select month: $month before current ${today.getMonth}") *>
                ZIO.fail(new RuntimeException(s"Can't select month: $month before current ${today.getMonth}"))
            }

            dateButtons <- SchedulerMarkup.monthKeyboard(month)
            _ <- telegramApi.sendInlineGroupButtons(context.chatId, "Укажи дату публикации расклада", dateButtons)
          } yield ()     
        case ScheduleCommand.SelectDate(date) =>
          for {
            _ <- ZIO.logInfo(s"Select date $date from chat ${context.chatId}")

            projectConfig <- ZIO.serviceWith[BotEnv](_.config.project)
            today <- DateTimeService.currentLocalDate()
            _ <- ZIO.unless(CalendarService.isDayEnable(today, date, projectConfig.maxFutureTime)) {
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
        case ScheduleCommand.SelectCardOfDay(delay) =>
          selectCardOfDayDelay(context, delay)(telegramApi, tarotApi, sessionService)
        case ScheduleCommand.Confirm =>
          confirmDateTime(context)(telegramApi, tarotApi, sessionService)
      }

  private def selectTime(context: TelegramContext, time: LocalTime)
      (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      _ <- ZIO.logInfo(s"Select time $time from chat ${context.chatId}")
      
      _ <- sessionService.setTime(context.chatId, time)

      session <- sessionService.get(context.chatId)
      date <- ZIO.fromOption(session.date)
        .orElseFail(new RuntimeException(s"Date not found in session for chat ${context.chatId}"))

      _ <- showDelayKeyboard(context, date, 0)(telegramApi)
    } yield ()

  private def selectCardOfDayDelay(context: TelegramContext, delay: Duration)
    (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      _ <- ZIO.logInfo(s"Select card of day delay $delay from chat ${context.chatId}")

      _ <- sessionService.setCardOfDayDelay(context.chatId, delay)

      session <- sessionService.get(context.chatId)
      date <- ZIO.fromOption(session.date)
        .orElseFail(new RuntimeException(s"Date not found in session for chat ${context.chatId}"))
      time <- ZIO.fromOption(session.time)
        .orElseFail(new RuntimeException(s"Time not found in session for chat ${context.chatId}"))
      cardOfDayDelay <- ZIO.fromOption(session.cardOfDayDelay)
        .orElseFail(new RuntimeException(s"Card of day delay not found in session for chat ${context.chatId}"))

      month = YearMonth.of(date.getYear, date.getMonth)
      buttons = List(
        TelegramInlineKeyboardButton("Назад к дате", Some(SchedulerCommands.selectMonth(month))),
        TelegramInlineKeyboardButton("Подтвердить", Some(SchedulerCommands.Confirm))
      )

      text =
        s"Время публикации: ${DateFormatter.fromLocalDate(date)} ${DateFormatter.fromLocalTime(time)} " +
          s"и картой дня через ${DateFormatter.fromDuration(cardOfDayDelay)}. Подтвердить?"
      _ <- telegramApi.sendInlineButtons(context.chatId, text, buttons)
    } yield ()

  private def confirmDateTime(context: TelegramContext)
      (telegramApi: TelegramApiService, tarotApi: TarotApiService, sessionService: BotSessionService) =
    for {
      _ <- ZIO.logInfo(s"Confirm datetime from chat ${context.chatId}")

      projectConfig <- ZIO.serviceWith[BotEnv](_.config.project)
      session <- sessionService.get(context.chatId)
      token <- ZIO.fromOption(session.token)
        .orElseFail(new RuntimeException(s"Token not found for chat ${context.chatId}"))
      spreadId <- ZIO.fromOption(session.spreadId)
        .orElseFail(new RuntimeException(s"SpreadId not found for chat ${context.chatId}"))
      date <- ZIO.fromOption(session.date)
        .orElseFail(new RuntimeException(s"Date not found in session for chat ${context.chatId}"))
      time <- ZIO.fromOption(session.time)
        .orElseFail(new RuntimeException(s"Time not found in session for chat ${context.chatId}"))
      cardOfDayDelay <- ZIO.fromOption(session.cardOfDayDelay)
        .orElseFail(new RuntimeException(s"Card of day delay not found in session for chat ${context.chatId}"))

      dateTime = LocalDateTime.of(date, time)
      today <- DateTimeService.currentLocalDateTime()
      _ <- ZIO.unless(CalendarService.isTimeEnable(today, dateTime, projectConfig.maxFutureTime)) {
        ZIO.logError(s"Can't select datetime: $dateTime before current $today") *>
          ZIO.fail(new RuntimeException(s"Can't select date: $date before current $today"))
      }

      zoneOffset <- DateTimeService.getOffset
      scheduledAt = dateTime.toInstant(zoneOffset)
      request = SpreadPublishRequest(scheduledAt, cardOfDayDelay)
      _ <- tarotApi.publishSpread(request, spreadId, token)

      text = s"Расклад будет опубликован ${DateFormatter.fromLocalDateTime(dateTime)} c картой дня через ${DateFormatter.fromDuration(cardOfDayDelay)}"
      _ <- telegramApi.sendText(context.chatId, text)
      _ <- sessionService.reset(context.chatId)
      _ <- SpreadFlow.selectSpreads(context)(telegramApi, tarotApi, sessionService)
    } yield ()

  private def showTimeKeyboard(context: TelegramContext, date: LocalDate, page: Int)
      (telegramApi: TelegramApiService): ZIO[BotEnv, Throwable, Unit] =
    for {
      dateButtons <- SchedulerMarkup.timeKeyboard(date, page)
      _ <- telegramApi.sendInlineGroupButtons(context.chatId, "Укажи время публикации расклада", dateButtons)
    } yield ()

  private def showDelayKeyboard(context: TelegramContext, date: LocalDate, page: Int)
      (telegramApi: TelegramApiService): ZIO[BotEnv, Throwable, Unit] =
    for {
      dateButtons <- SchedulerMarkup.delayKeyboard(date, page)
      _ <- telegramApi.sendInlineGroupButtons(context.chatId, "Укажи время, через которое будет опубликована карта дня", dateButtons)
    } yield ()
}
