package bot.infrastructure.services.datetime

import java.time.format.DateTimeFormatter
import java.time.*

object DateFormatter {
  def getDate(date: LocalDate): String = {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    date.format(dateFormatter)
  }

  def getTime(time: LocalTime): String = {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    time.format(timeFormatter)
  }

  def getDateTime(dateTime: LocalDateTime): String = {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    dateTime.format(dateTimeFormatter)
  }
}
