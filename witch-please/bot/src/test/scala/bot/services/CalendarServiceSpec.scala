package bot.services

import bot.infrastructure.services.calendar.CalendarService
import bot.integration.BotIntegrationSpec.suite
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

import java.time.{LocalDate, LocalDateTime, LocalTime, YearMonth}

object CalendarServiceSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment & Scope, Any] = suite("CalendarModel")(
    test("prev is disabled for current month") {
      val today = LocalDate.of(2025, 10, 15)
      val calendar = CalendarService.buildMonth(today, YearMonth.of(today.getYear, today.getMonth))
      assertTrue(!calendar.month.prevEnabled, calendar.month.nextEnabled)
    },

    test("past days are disabled, future available") {
      val currentDay = 15
      val today = LocalDate.of(2025, 10, currentDay)
      val calendar = CalendarService.buildMonth(today, YearMonth.of(today.getYear, today.getMonth))
      val pastDays = calendar.days.filter ( day => !day.enabled )
      val availDays = calendar.days.filter ( day => day.enabled )
      assertTrue(pastDays.forall(_.day < currentDay), availDays.headOption.map(_.day).contains(currentDay))
    },

    test("filters out past slots for today") {
      val today = LocalDateTime.of(2025, 10, 9, 10, 15)
      val date  = today.toLocalDate

      val grid = CalendarService.buildTime(today, date)

      val allTimes = grid.slots.map(_.time)
      val firstEnabled = allTimes.find(t => t.isAfter(today.toLocalTime))

      assertTrue(
        firstEnabled.nonEmpty,
        grid.slots.forall(slot => slot.time.isAfter(today.toLocalTime) || !slot.enabled)
      )
    },

    test("enables all slots for future date") {
      val today = LocalDateTime.of(2025, 10, 9, 22, 0)
      val tomorrow = today.plusDays(1).toLocalDate

      val grid = CalendarService.buildTime(today, tomorrow)

      assertTrue(grid.slots.forall(_.enabled))
    },

    test("returns first page with correct slicing") {
      val pageSize = 5
      val today = LocalDateTime.of(2025, 10, 9, 10, 0)
      val date  = today.toLocalDate

      val grid = CalendarService.buildTime(today, date, pageSize = pageSize)

      assertTrue(
        grid.slots.size == pageSize,
        grid.time.page == 0,
        grid.time.totalPages > 1
      )
    },

    test("respects safePage bounds") {
      val today = LocalDateTime.of(2025, 10, 9, 10, 0)
      val date  = today.toLocalDate

      val gridNeg = CalendarService.buildTime(today, date, page = -1)
      val gridOver = CalendarService.buildTime(today, date, page = 999)

      assertTrue(
        gridNeg.time.page == 0,
        gridOver.time.page == gridOver.time.totalPages - 1
      )
    },

    test("supports night window crossing midnight") {
      val today = LocalDateTime.of(2025, 10, 9, 12, 0)
      val date = LocalDate.of(2025, 10, 9)
      val grid = CalendarService.buildTime(today, date, start = LocalTime.of(8, 0), end = LocalTime.of(2, 0), pageSize = 1000)

      val times = grid.slots.map(_.time)
      assertTrue(times.contains(LocalTime.of(0, 30)), times.contains(LocalTime.of(23, 30)))
    }
  )
}
