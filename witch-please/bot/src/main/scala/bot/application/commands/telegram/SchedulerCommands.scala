package bot.application.commands.telegram

import java.time.*

object SchedulerCommands {
  final val Prefix = "/scheduler"
  final val SelectMonth = s"${Prefix}_month"
  final val SelectDate = s"${Prefix}_date"
  final val SelectTimePage = s"${Prefix}_time_page"
  final val SelectTime = s"${Prefix}_time"
  final val Confirm = s"${Prefix}_confirm"

  def selectMonth(month: YearMonth) = s"$SelectMonth $month"
  def selectDate(date: LocalDate) = s"$SelectDate $date"
  def selectTimePage(page: Int) = s"$SelectTimePage $page"
  def selectTime(time: LocalTime) = s"$SelectTime $time"
  def confirm(dateTime: LocalDateTime) = s"$SelectTime $dateTime"
}
