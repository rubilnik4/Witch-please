package bot.infrastructure.services.datetime

import shared.infrastructure.services.common.DateTimeService

import java.time.format.DateTimeFormatter
import java.time.*

object DateFormatter {
  def fromLocalDate(date: LocalDate): String = {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    date.format(dateFormatter)
  }

  def fromLocalTime(time: LocalTime): String = {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    time.format(timeFormatter)
  }

  def fromLocalDateTime(dateTime: LocalDateTime): String = {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    dateTime.format(dateTimeFormatter)
  }

  def fromInstant(dateTime: Instant): String = {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(DateTimeService.Zone)
    dateTimeFormatter.format(dateTime)
  }

  def fromDuration(duration: Duration): String = {
    val totalMinutes = duration.toMinutes
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    f"$hours%02d:$minutes%02d"
  }
}
