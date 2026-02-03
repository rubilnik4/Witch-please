package bot.application.handlers.telegram.markup

import bot.application.commands.telegram.{SchedulerCommands, TelegramCommands}
import bot.domain.models.calendar.*
import bot.infrastructure.services.calendar.CalendarService
import bot.layers.BotEnv
import shared.api.dto.telegram.*
import shared.infrastructure.services.common.DateTimeService
import zio.ZIO

import java.time.{Duration, LocalDate, LocalDateTime, LocalTime, YearMonth}

object SchedulerMarkup {
  def monthKeyboard(month: YearMonth): ZIO[BotEnv, Throwable, List[List[TelegramInlineKeyboardButton]]] =
    for {
      projectConfig <- ZIO.serviceWith[BotEnv](_.config.project)

      today <- DateTimeService.currentLocalDate()
      calendar = CalendarService.buildMonth(today, month, projectConfig.maxFutureTime)
    } yield keyboardDate(calendar)

  def timeKeyboard(date: LocalDate, page: Int): ZIO[BotEnv, Throwable, List[List[TelegramInlineKeyboardButton]]] =
    for {
      projectConfig <- ZIO.serviceWith[BotEnv](_.config.project)

      today <- DateTimeService.currentLocalDateTime()
      calendarTime = CalendarService.buildTime(today, date, projectConfig.maxFutureTime, page = page)
    } yield keyboardTime(calendarTime)

  def delayKeyboard(date: LocalDate, page: Int): ZIO[BotEnv, Throwable, List[List[TelegramInlineKeyboardButton]]] =
    for {
      projectConfig <- ZIO.serviceWith[BotEnv](_.config.project)
      maxDelay = projectConfig.maxCardOfDayDelay

      today <- DateTimeService.currentLocalDateTime()
      calendarTime = CalendarService.buildTime(LocalDateTime.of(date, LocalTime.of(0, 0)), date, Duration.ofDays(1),
        page = page, start = LocalTime.of(0, 0), end = LocalTime.MIDNIGHT.plus(maxDelay).plusMinutes(1))
    } yield keyboardDelay(calendarTime)

  private def keyboardDate(calendar: Calendar): List[List[TelegramInlineKeyboardButton]] = {
    val monthKeyboard = getMonthKeyboard(calendar.month)
    val dayKeyboard = getDaysKeyboard(calendar.days)
    monthKeyboard :: dayKeyboard
  }

  private def getMonthKeyboard(calendarMonth: CalendarMonth) = {
    val prevButton =
      if (calendarMonth.prevEnabled)
        TelegramInlineKeyboardButton("◀️", Some(SchedulerCommands.selectMonth(calendarMonth.prevMonth)))
      else TelegramInlineKeyboardButton(" ", Some(TelegramCommands.StubCommand))

    val title = TelegramInlineKeyboardButton(calendarMonth.title, Some(TelegramCommands.StubCommand))

    val nextButton =
      if (calendarMonth.nextEnabled)
        TelegramInlineKeyboardButton("▶️", Some(SchedulerCommands.selectMonth(calendarMonth.nextMonth)))
      else TelegramInlineKeyboardButton(" ", Some(TelegramCommands.StubCommand))

    List(prevButton, title, nextButton)
  }

  private def getDaysKeyboard(calendarDays: List[CalendarDay]) =
    calendarDays
      .filter(_.enabled)
      .map { day =>
        TelegramInlineKeyboardButton(f"${day.day}%2d", Some(SchedulerCommands.selectDate(day.date)))
      }
      .grouped(7).map(_.toList).toList

  private def keyboardTime(timeGrid: CalendarTimeGrid): List[List[TelegramInlineKeyboardButton]] = {
    val timeKeyboard = getTimeKeyboard(timeGrid.time)
    val slotKeyboard = getTimeSlotKeyboard(timeGrid.slots, SchedulerCommands.selectTime)
    timeKeyboard :: slotKeyboard
  }

  private def keyboardDelay(timeGrid: CalendarTimeGrid): List[List[TelegramInlineKeyboardButton]] = {
    val timeKeyboard = getTimeKeyboard(timeGrid.time)
    val selectDelay: LocalTime => String = time => {
      val delay = Duration.between(LocalTime.MIDNIGHT, time)
      SchedulerCommands.selectCardOfDayDelay(delay)
    }
    val slotKeyboard = getTimeSlotKeyboard(timeGrid.slots, selectDelay)
    timeKeyboard :: slotKeyboard
  }

  private def getTimeKeyboard(calendarTime: CalendarTime) = {
    val pagePrevButton =
      if (calendarTime.page > 0)
        TelegramInlineKeyboardButton("◀️", Some(SchedulerCommands.selectTimePage(calendarTime.page - 1)))
      else TelegramInlineKeyboardButton(" ", Some(TelegramCommands.StubCommand))

    val pageLabel = s"${calendarTime.page + 1}/${calendarTime.totalPages}"
    val pageButton = TelegramInlineKeyboardButton(pageLabel, Some(TelegramCommands.StubCommand))

    val pageNextButton =
      if (calendarTime.page < calendarTime.totalPages - 1)
        TelegramInlineKeyboardButton("▶️", Some(SchedulerCommands.selectTimePage(calendarTime.page + 1)))
      else TelegramInlineKeyboardButton(" ", Some(TelegramCommands.StubCommand))
    val pageButtons = List(pagePrevButton, pageButton, pageNextButton)
    
    val month = YearMonth.of(calendarTime.date.getYear, calendarTime.date.getMonth)
    val returnButton = TelegramInlineKeyboardButton("⬅ Дата", Some(SchedulerCommands.selectMonth(month)))
    returnButton :: pageButtons
  }

  private def getTimeSlotKeyboard(calendarSlots: List[CalendarTimeSlot], slotCommand: LocalTime => String) =
    calendarSlots
      .map { slot =>
        val label = f"${slot.time.getHour}%02d:${slot.time.getMinute}%02d"
        TelegramInlineKeyboardButton(label, Some(slotCommand(slot.time)))
      }
      .grouped(5).map(_.toList).toList
}
