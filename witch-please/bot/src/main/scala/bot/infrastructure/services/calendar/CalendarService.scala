package bot.infrastructure.services.calendar

import bot.domain.models.calendar.*
import shared.infrastructure.services.common.DateTimeService

import java.time.*

object CalendarService {
  def buildMonth(today: LocalDate, month: YearMonth, maxFuture: Duration): Calendar = {
    val prevMonth = month.minusMonths(1)
    val nextMonth = month.plusMonths(1)
    val prevEnabled = isPrevMonthEnable(today, prevMonth)
    val nextEnabled  = isNextMonthEnable(today, nextMonth, maxFuture)
    val title = s"${month.getMonth.toString.toLowerCase.capitalize} ${month.getYear}"
    val calendarMonth = CalendarMonth(title, prevEnabled, nextEnabled, prevMonth, nextMonth)

    val calendarDays =
      (1 to month.lengthOfMonth).toList.map { day =>
        val date = month.atDay(day)
        CalendarDay(day, date, isDayEnable(today, date, maxFuture))
      }
    Calendar(calendarMonth, calendarDays)
  }

  def buildTime(today: LocalDateTime, date: LocalDate, maxFuture: Duration,
      stepMinutes: CalendarTimeStep = CalendarTimeStep.M30,
      start: LocalTime = LocalTime.of(8, 0), end: LocalTime = LocalTime.of(2, 0),
      page: Int = 0, pageSize: Int = 20): CalendarTimeGrid = {

    val slots = getCalendarTimes(today, date, maxFuture, stepMinutes, start, end).map (time =>
      CalendarTimeSlot(time))

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

  def isNextMonthEnable(today: LocalDate, month: YearMonth, maxFuture: Duration): Boolean = {
    val maxDate = getMaxDate(today, maxFuture)
    val monthStart = month.atDay(1)
    val monthEnd = month.atEndOfMonth()
    !monthStart.isAfter(maxDate) && !monthEnd.isBefore(today)
  }

  def isDayEnable(today: LocalDate, date: LocalDate, maxFuture: Duration): Boolean = {
    val maxDate = getMaxDate(today, maxFuture)
    !date.isBefore(today) && !date.isAfter(maxDate)
  }

  def isTimeEnable(today: LocalDateTime, time: LocalDateTime, maxFuture: Duration): Boolean = {
    val maxDateTime = getMaxDateTime(today, maxFuture)
    !time.isBefore(today) && !time.isAfter(maxDateTime)
  }

  def getMaxDate(today: LocalDate, maxFuture: Duration): LocalDate = {
    val start = today.atStartOfDay(DateTimeService.Zone).toInstant
    val max = start.plus(maxFuture)
    LocalDateTime.ofInstant(max, DateTimeService.Zone).toLocalDate
  }

  def getMaxDateTime(today: LocalDateTime, maxFuture: Duration): LocalDateTime = {
    val start = today.atZone(DateTimeService.Zone)
    start.plus(maxFuture).toLocalDateTime
  }

  private def getCalendarTimes(today: LocalDateTime, date: LocalDate, maxFuture: Duration,
                               stepMinutes: CalendarTimeStep, start: LocalTime, end: LocalTime) =
    val dayStart = date.atStartOfDay()
    val dayEnd = date.plusDays(1).atStartOfDay()

    val segments =
      if (end.isBefore(start)) List((dayStart, date.atTime(end)), (date.atTime(start), dayEnd))
      else List((date.atTime(start), date.atTime(end)))

    val maxDateTime = getMaxDateTime(today, maxFuture)

    segments.flatMap { case (rawStart, rawEnd) =>
      getCalendarSlots(today, maxDateTime, maxFuture, stepMinutes, rawStart, rawEnd)
    }.distinct.sortBy(time => time)

  private def getCalendarSlots(today: LocalDateTime, maxDateTime: LocalDateTime,maxFuture: Duration,
      step: CalendarTimeStep, rawStart: LocalDateTime, rawEnd: LocalDateTime): List[LocalTime] = {
    val segStartClamped = if (rawStart.isBefore(today)) today else rawStart
    val segEndClamped = if (rawEnd.isAfter(maxDateTime)) maxDateTime else rawEnd

    val segStart = CalendarTimeStep.alignUp(segStartClamped, step)
    val segEnd = CalendarTimeStep.alignDown(segEndClamped, step)

    if (!segStart.isBefore(segEnd)) Nil
    else
      slotsInRange(step, segStart, segEnd)
        .filter(dt => isTimeEnable(today, dt, maxFuture))
        .map(_.toLocalTime)
  }

  private def slotsInRange(stepMinutes: CalendarTimeStep, start: LocalDateTime, end: LocalDateTime): List[LocalDateTime] =
    Iterator.iterate(start)(_.plusMinutes(stepMinutes.minutes.toLong))
      .takeWhile(_.isBefore(end))
      .toList
}
