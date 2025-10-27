package bot.application.handlers.telegram.markup

import bot.application.commands.telegram.{SchedulerCommands, TelegramCommands}
import bot.domain.models.calendar.Calendar
import bot.infrastructure.services.calendar.CalendarService
import shared.api.dto.telegram.*
import shared.infrastructure.services.common.DateTimeService
import zio.UIO

import java.time.YearMonth

object SchedulerMarkup {
  def monthKeyboard(month: YearMonth = YearMonth.now()): UIO[List[List[TelegramInlineKeyboardButton]]] =
    for {
      today <- DateTimeService.currentLocalDate()
      calendar = CalendarService.buildMonth(today, month)
    } yield keyboardMonth(calendar)

  private def keyboardMonth(calendar: Calendar): List[List[TelegramInlineKeyboardButton]] = {
    val monthKeyboard = getMonthKeyboard(calendar)
    val dayKeyboard = getDaysKeyboard(calendar)
    monthKeyboard :: dayKeyboard
  }

  private def getMonthKeyboard(calendar: Calendar) = {
    val prevButton =
      if (calendar.month.prevEnabled)
        TelegramInlineKeyboardButton("◀️", Some(SchedulerCommands.selectMonth(calendar.month.prevMonth)))
      else TelegramInlineKeyboardButton(" ", Some(TelegramCommands.StubCommand))

    val title = TelegramInlineKeyboardButton(calendar.month.title, Some(TelegramCommands.StubCommand))

    val nextButton =
      if (calendar.month.nextEnabled)
        TelegramInlineKeyboardButton("▶️", Some(SchedulerCommands.selectMonth(calendar.month.nextMonth)))
      else TelegramInlineKeyboardButton(" ", Some(TelegramCommands.StubCommand))

    List(prevButton, title, nextButton)
  }

  private def getDaysKeyboard(calendar: Calendar) =
    calendar.days.filter(_.enabled).map { day =>
      TelegramInlineKeyboardButton(f"${day.day}%2d", Some(SchedulerCommands.selectDate(day.date)))
    }.grouped(7).map(_.toList).toList
}
