package bot.domain.models.calendar

final case class Calendar (
  month: CalendarMonth,
  days: List[CalendarDay]
)
