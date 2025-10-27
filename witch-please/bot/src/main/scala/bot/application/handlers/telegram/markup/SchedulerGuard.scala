package bot.application.handlers.telegram.markup

import shared.infrastructure.services.common.DateTimeService
import zio.UIO

import java.time.*

object SchedulerGuard {
  private final val Zone = ZoneId.of("Europe/Moscow")

  def canNavigatePrevMonth(currentMonth: YearMonth, zone: ZoneId = Zone): UIO[Boolean] =
    DateTimeService.currentYearMonth(zone).map(month => currentMonth.isAfter(month) || currentMonth.equals(month))

  def canNavigatePrevDay(currentDay: LocalDate, zone: ZoneId = Zone): UIO[Boolean] =
    DateTimeService.currentLocalDate(zone).map(today => !currentDay.isBefore(today))
}