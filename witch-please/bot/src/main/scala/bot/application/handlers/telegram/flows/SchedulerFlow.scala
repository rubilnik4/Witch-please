package bot.application.handlers.telegram.flows

import bot.application.commands.ScheduleCommand
import bot.application.commands.telegram.SchedulerCommands
import bot.application.handlers.telegram.markup.SchedulerMarkup
import bot.domain.models.telegram.TelegramContext
import bot.infrastructure.services.calendar.CalendarService
import bot.infrastructure.services.datetime.DateFormatter
import bot.infrastructure.services.sessions.SessionRequire
import bot.layers.BotEnv
import shared.api.dto.tarot.spreads.SpreadPublishRequest
import shared.api.dto.telegram.TelegramInlineKeyboardButton
import shared.infrastructure.services.common.DateTimeService
import zio.ZIO

import java.time.{Duration, LocalDate, LocalDateTime, LocalTime, YearMonth}

object SchedulerFlow {
  def handle(context: TelegramContext, command: ScheduleCommand): ZIO[BotEnv, Throwable, Unit] =
      command match {
        case ScheduleCommand.SelectMonth(month) =>
          selectMonth(context, month)
        case ScheduleCommand.SelectDate(date) =>
          selectDate(context, date)
        case ScheduleCommand.SelectTimePage(page) =>
          for {
            _ <- ZIO.logInfo(s"Select time page $page from chat ${context.chatId}")

            date <- SessionRequire.date(context.chatId)
            _ <- showTimeKeyboard(context, date, page)
          } yield ()
      case ScheduleCommand.SelectTime(time) =>
        selectTime(context, time)
      case ScheduleCommand.SelectCardOfDay(delay) =>
        selectCardOfDayDelay(context, delay)
        case ScheduleCommand.Confirm =>
          confirmDateTime(context)
      }

  private def selectMonth(context: TelegramContext, month: YearMonth): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Select month $month from chat ${context.chatId}")

      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
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

  private def selectDate(context: TelegramContext, date: LocalDate): ZIO[BotEnv, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo(s"Select date $date from chat ${context.chatId}")

      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      projectConfig <- ZIO.serviceWith[BotEnv](_.config.project)
      today <- DateTimeService.currentLocalDate()
      _ <- ZIO.unless(CalendarService.isDayEnable(today, date, projectConfig.maxFutureTime)) {
        ZIO.logError(s"Can't select date: $date before current $today") *>
          ZIO.fail(new RuntimeException(s"Can't select date: $date before current $today"))
      }

      _ <- sessionService.setDate(context.chatId, date)
      _ <- showTimeKeyboard(context, date, 0)
    } yield ()

  private def selectTime(context: TelegramContext, time: LocalTime) =
    for {
      _ <- ZIO.logInfo(s"Select time $time from chat ${context.chatId}")

      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      _ <- sessionService.setTime(context.chatId, time)

      date <- SessionRequire.date(context.chatId)
      _ <- showDelayKeyboard(context, date, 0)
    } yield ()

  private def selectCardOfDayDelay(context: TelegramContext, delay: Duration) =
    for {
      _ <- ZIO.logInfo(s"Select card of day delay $delay from chat ${context.chatId}")

      sessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      _ <- sessionService.setCardOfDayDelay(context.chatId, delay)

      date <- SessionRequire.date(context.chatId)
      time <- SessionRequire.time(context.chatId)
      cardOfDayDelay <- SessionRequire.cardOfDayDelay(context.chatId)

      month = YearMonth.of(date.getYear, date.getMonth)
      buttons = List(
        TelegramInlineKeyboardButton("Назад к дате", Some(SchedulerCommands.selectMonth(month))),
        TelegramInlineKeyboardButton("Подтвердить", Some(SchedulerCommands.Confirm))
      )

      text = s"Время публикации: ${DateFormatter.fromLocalDate(date)} ${DateFormatter.fromLocalTime(time)} " +
          s"и картой дня через ${DateFormatter.fromDuration(cardOfDayDelay)}. Подтвердить?"
      _ <- telegramApi.sendInlineButtons(context.chatId, text, buttons)
    } yield ()

  private def confirmDateTime(context: TelegramContext) =
    for {
      _ <- ZIO.logInfo(s"Confirm datetime from chat ${context.chatId}")

      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      tarotApi <- ZIO.serviceWith[BotEnv](_.services.tarotApiService)
      projectConfig <- ZIO.serviceWith[BotEnv](_.config.project)
      spread <- SessionRequire.spread(context.chatId)
      token <- SessionRequire.token(context.chatId)
      date <- SessionRequire.date(context.chatId)
      time <- SessionRequire.time(context.chatId)
      cardOfDayDelay <- SessionRequire.cardOfDayDelay(context.chatId)

      dateTime = LocalDateTime.of(date, time)
      today <- DateTimeService.currentLocalDateTime()
      _ <- ZIO.unless(CalendarService.isTimeEnable(today, dateTime, projectConfig.maxFutureTime)) {
        ZIO.logError(s"Can't select datetime: $dateTime before current $today") *>
          ZIO.fail(new RuntimeException(s"Can't select date: $date before current $today"))
      }

      zoneOffset <- DateTimeService.getOffset
      scheduledAt = dateTime.toInstant(zoneOffset)
      request = SpreadPublishRequest(scheduledAt, cardOfDayDelay)
      _ <- tarotApi.publishSpread(request, spread.spreadId, token)

      text = s"Расклад будет опубликован ${DateFormatter.fromLocalDateTime(dateTime)} c картой дня через ${DateFormatter.fromDuration(cardOfDayDelay)}"
      _ <- telegramApi.sendText(context.chatId, text)
      _ <- SpreadFlow.selectSpread(context, spread.spreadId)
    } yield ()

  private def showTimeKeyboard(context: TelegramContext, date: LocalDate, page: Int) =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      dateButtons <- SchedulerMarkup.timeKeyboard(date, page)
      _ <- telegramApi.sendInlineGroupButtons(context.chatId, "Укажи время публикации расклада", dateButtons)
    } yield ()

  private def showDelayKeyboard(context: TelegramContext, date: LocalDate, page: Int) =
    for {
      telegramApi <- ZIO.serviceWith[BotEnv](_.services.telegramApiService)
      dateButtons <- SchedulerMarkup.delayKeyboard(date, page)
      _ <- telegramApi.sendInlineGroupButtons(context.chatId, "Укажи время, через которое будет опубликована карта дня", dateButtons)
    } yield ()
}
