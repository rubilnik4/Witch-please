package bot.domain.models.calendar

import java.time.{LocalDate, LocalDateTime, LocalTime}

final case class CalendarTime(
  date: LocalDate,
  today: LocalDateTime,                       
  page: Int,
  totalPages: Int,
)