package bot.domain.models.calendar

import java.time.LocalDate

final case class CalendarDay(
  day: Int,
  date: LocalDate,
  enabled: Boolean,
)
