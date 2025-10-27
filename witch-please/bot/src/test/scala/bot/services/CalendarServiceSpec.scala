package bot.services

import bot.infrastructure.services.calendar.CalendarService
import bot.integration.BotIntegrationSpec.suite
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

import java.time.{LocalDate, YearMonth}

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
    }
  )
}
