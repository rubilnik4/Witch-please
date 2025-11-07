package bot.services

import bot.domain.models.calendar.CalendarTimeStep
import bot.infrastructure.services.calendar.CalendarService
import bot.integration.BotIntegrationSpec.suite
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

import java.time.*

object CalendarServiceSpec extends ZIOSpecDefault {
  private val maxFuture: Duration = Duration.ofDays(90)

  def spec: Spec[TestEnvironment & Scope, Any] = suite("CalendarModel")(
    test("prev is disabled for current month") {
      val today = LocalDate.of(2025, 10, 15)
      val calendar = CalendarService.buildMonth(today, YearMonth.of(today.getYear, today.getMonth), maxFuture)
      assertTrue(!calendar.month.prevEnabled, calendar.month.nextEnabled)
    },

    test("past days are disabled, future available") {
      val currentDay = 15
      val today = LocalDate.of(2025, 10, currentDay)
      val calendar = CalendarService.buildMonth(today, YearMonth.of(today.getYear, today.getMonth), maxFuture)
      val pastDays = calendar.days.filter ( day => !day.enabled )
      val availDays = calendar.days.filter ( day => day.enabled )
      assertTrue(pastDays.forall(_.day < currentDay), availDays.headOption.map(_.day).contains(currentDay))
    },

    test("next month is disabled beyond maxFuture window") {
      val today = LocalDate.of(2025, 10, 15)
      val limit = CalendarService.getMaxDate(today, maxFuture)
      val limitMonth = YearMonth.from(limit)

      val calendarAtLimit = CalendarService.buildMonth(today, limitMonth, maxFuture)

      assertTrue(!calendarAtLimit.month.nextEnabled)
    },

    test("filters out past slots for today") {
      val today = LocalDateTime.of(2025, 10, 9, 10, 15)
      val date = today.toLocalDate

      val grid = CalendarService.buildTime(today, date, maxFuture)

      val allTimes = grid.slots.map(_.time)
      val firstEnabled = allTimes.find(time => !time.isBefore(today.toLocalTime))

      assertTrue(
        firstEnabled.nonEmpty,
        grid.slots.forall(slot => !slot.time.isBefore(today.toLocalTime))
      )
    },

    test("enables all slots for future date  inside limit") {
      val today = LocalDateTime.of(2025, 10, 9, 22, 0)
      val tomorrow = today.plusDays(1).toLocalDate

      val grid = CalendarService.buildTime(today, tomorrow, maxFuture)

      assertTrue(grid.slots.nonEmpty)
    },

    test("disables slots beyond maxFuture on the limit date") {
      val today = LocalDateTime.of(2025, 10, 9, 14, 0)

      val limit = CalendarService.getMaxDateTime(today, maxFuture)
      val limitDate = limit.toLocalDate
      val limitTime = limit.toLocalTime

      val grid = CalendarService.buildTime(today, limitDate, maxFuture, pageSize = 999)
      val slots = grid.slots

      assertTrue(
        slots.nonEmpty,
        slots.forall(slot => !slot.time.isAfter(limitTime))
      )
    },

    test("returns first page with correct slicing") {
      val pageSize = 5
      val today = LocalDateTime.of(2025, 10, 9, 10, 0)
      val date = today.toLocalDate

      val grid = CalendarService.buildTime(today, date, maxFuture, pageSize = pageSize)

      assertTrue(
        grid.slots.size == pageSize,
        grid.time.page == 0,
        grid.time.totalPages > 1
      )
    },

    test("respects safePage bounds") {
      val today = LocalDateTime.of(2025, 10, 9, 10, 0)
      val date  = today.toLocalDate

      val gridNeg = CalendarService.buildTime(today, date, maxFuture, page = -1)
      val gridOver = CalendarService.buildTime(today, date, maxFuture, page = 999)

      assertTrue(
        gridNeg.time.page == 0,
        gridOver.time.page == gridOver.time.totalPages - 1
      )
    },

    test("night window splits across days: current date has only evening part") {
      val today = LocalDateTime.of(2025, 10, 9, 12, 0)
      val date  = LocalDate.of(2025, 10, 9)

      val grid = CalendarService.buildTime(today, date, maxFuture,
        start = LocalTime.of(8, 0), end = LocalTime.of(2, 0), pageSize = 1000)

      val times = grid.slots.map(_.time)
      assertTrue(
        !times.contains(LocalTime.of(0, 30)),
        times.contains(LocalTime.of(23, 30))
      )
    },

    test("night window: next date shows the early-morning tail") {
      val today    = LocalDateTime.of(2025, 10, 9, 12, 0)
      val nextDate = LocalDate.of(2025, 10, 10)

      val gridNext = CalendarService.buildTime(today, nextDate, maxFuture,
        start = LocalTime.of(8, 0), end = LocalTime.of(2, 0), pageSize = 1000)

      val timesNext = gridNext.slots.map(_.time)
      assertTrue(timesNext.contains(LocalTime.of(0, 30)))
    },

    test("quantizes start up and end down (end-exclusive)") {
      val today = LocalDateTime.of(2025, 10, 9, 8, 0)
      val date  = LocalDate.of(2025, 10, 9)

      val grid = CalendarService.buildTime(today, date, maxFuture,
        stepMinutes = CalendarTimeStep.M30, start = LocalTime.of(8, 7), end = LocalTime.of(10, 2), pageSize = 1000)

      val times = grid.slots.map(_.time)
      assertTrue(
        times == List(LocalTime.of(8,30), LocalTime.of(9,0), LocalTime.of(9,30))
      )
    },

    test("quantizes segments in night window (wrap across midnight)") {
      val today = LocalDateTime.of(2025, 10, 9, 12, 0)
      val date  = LocalDate.of(2025, 10, 9)

      val gridCurrent = CalendarService.buildTime(today, date, maxFuture,
        stepMinutes = CalendarTimeStep.M60, start = LocalTime.of(23, 17), end = LocalTime.of(2, 13), pageSize = 1000)
      val timesCurrent = gridCurrent.slots.map(_.time)

      val gridNext = CalendarService.buildTime(today, date.plusDays(1), maxFuture,
        stepMinutes = CalendarTimeStep.M60, start = LocalTime.of(23, 17), end = LocalTime.of(2, 13), pageSize = 1000)
      val timesNext = gridNext.slots.map(_.time)

      assertTrue(
        timesCurrent.isEmpty,
        timesNext == List(LocalTime.of(0,0), LocalTime.of(1,0))
      )
    }
  )
}
