package bot.domain.models.calendar

import java.time.LocalDate

final case class CalendarTime(
  date: LocalDate,
  page: Int,
  totalPages: Int,
)