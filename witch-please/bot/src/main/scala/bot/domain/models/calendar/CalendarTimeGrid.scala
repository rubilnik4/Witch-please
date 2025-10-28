package bot.domain.models.calendar

final case class CalendarTimeGrid(
  time: CalendarTime,
  slots: List[CalendarTimeSlot]
)

