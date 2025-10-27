package bot.application.commands.telegram

import java.time.*

object SchedulerCommands {
  final val Prefix = "/scheduler"
  final val SelectMonth = s"${Prefix}_month"
  final val SelectDate = s"${Prefix}_date"
  final val SelectTime = s"${Prefix}_time"
  final val Confirm = s"${Prefix}_confirm"

  def selectMonth(month: YearMonth) = s"$SelectMonth $month"
  def selectDate(date: LocalDate) = s"$SelectDate $date"
  def selectTime(time: LocalTime) = s"$SelectTime $time"
}
