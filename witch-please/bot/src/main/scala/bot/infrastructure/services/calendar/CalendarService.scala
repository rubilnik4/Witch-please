package bot.infrastructure.services.calendar

import bot.domain.models.calendar.*
import shared.infrastructure.services.common.DateTimeService.getDateTimeNow
import zio.UIO

import java.time.{LocalDate, YearMonth, ZoneId, ZonedDateTime}

object CalendarService {
  def buildMonth(today: LocalDate, month: YearMonth): Calendar = {
    val prevMonth = month.minusMonths(1)
    val prevEnabled = canNavigatePrevMonth(today, prevMonth)
    val title = s"${month.getMonth.toString.toLowerCase.capitalize} ${month.getYear}"
    val calendarMonth = CalendarMonth(title, prevEnabled, true, prevMonth, month.plusMonths(1))

    val calendarDays =
      (1 to month.lengthOfMonth).toList.map { day =>
        val date = month.atDay(day)
        CalendarDay(day, date, canNavigatePrevDay(today, date))
      }
    Calendar(calendarMonth, calendarDays)
  }

  def canNavigatePrevMonth(today: LocalDate, month: YearMonth): Boolean =
    !month.isBefore(YearMonth.from(today))

  def canNavigatePrevDay(today: LocalDate, date: LocalDate): Boolean =
    !date.isBefore(today)
}
