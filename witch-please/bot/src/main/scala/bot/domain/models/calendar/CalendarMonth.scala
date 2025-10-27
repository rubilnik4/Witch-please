package bot.domain.models.calendar

import java.time.YearMonth

final case class CalendarMonth(
  title: String,
  prevEnabled: Boolean,
  nextEnabled: Boolean,
  prevMonth: YearMonth,
  nextMonth: YearMonth
)
