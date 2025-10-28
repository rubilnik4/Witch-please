package bot.infrastructure.services.calendar

import bot.domain.models.calendar.*

import java.time.*

object CalendarService {
  def buildMonth(today: LocalDate, month: YearMonth): Calendar = {
    val prevMonth = month.minusMonths(1)
    val prevEnabled = isPrevMonthEnable(today, prevMonth)
    val title = s"${month.getMonth.toString.toLowerCase.capitalize} ${month.getYear}"
    val calendarMonth = CalendarMonth(title, prevEnabled, true, prevMonth, month.plusMonths(1))

    val calendarDays =
      (1 to month.lengthOfMonth).toList.map { day =>
        val date = month.atDay(day)
        CalendarDay(day, date, isPrevDayEnable(today, date))
      }
    Calendar(calendarMonth, calendarDays)
  }

  def buildTime(today: LocalDateTime, date: LocalDate, stepMinutes: Int = 30,
      start: LocalTime = LocalTime.of(8, 0), end: LocalTime = LocalTime.of(2, 0),
      page: Int = 0, pageSize: Int = 20): CalendarTimeGrid = {

    val slots = getCalendarTimes(date: LocalDate, stepMinutes, start, end).map { time =>
      val isEnabled = isPrevTimeEnable(today, LocalDateTime.of(date, time))
      CalendarTimeSlot(time, isEnabled)
    }

    val totalPages = math.max(1, Math.ceil(slots.size.toDouble / pageSize).toInt)
    val safePage = math.max(0, math.min(page, totalPages - 1))
    val fromPage = safePage * pageSize
    val toPage = math.min(slots.size, fromPage + pageSize)
    val calendarSlots = slots.slice(fromPage, toPage)
    val calendarTime = CalendarTime(date, safePage, totalPages)

    CalendarTimeGrid(calendarTime, calendarSlots)
  }

  def isPrevMonthEnable(today: LocalDate, month: YearMonth): Boolean =
    !month.isBefore(YearMonth.from(today))

  def isPrevDayEnable(today: LocalDate, date: LocalDate): Boolean =
    !date.isBefore(today)

  def isPrevTimeEnable(today: LocalDateTime, time: LocalDateTime): Boolean =
    !time.isBefore(today)

  private def getCalendarTimes(date: LocalDate, stepMinutes: Int, start: LocalTime, end: LocalTime) =
    if (end.isBefore(start)) {      
      val startDatetime = date.atTime(start)
      val endDateTime = date.plusDays(1).atTime(end)
      slotsInRange(stepMinutes, startDatetime, endDateTime)
    } else {
      val startDatetime = date.atTime(start)
      val endDateTime = date.atTime(end)
      slotsInRange(stepMinutes, startDatetime, endDateTime)
    }

  private def slotsInRange(stepMinutes: Int, start: LocalDateTime, end: LocalDateTime): List[LocalTime] =
    Iterator.iterate(start)(_.plusMinutes(stepMinutes.toLong))
      .takeWhile(!_.isAfter(end))
      .map(_.toLocalTime)
      .toList
}
