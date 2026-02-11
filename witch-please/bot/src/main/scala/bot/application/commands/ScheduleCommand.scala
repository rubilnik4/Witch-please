package bot.application.commands

import java.time.*
import java.util.UUID

sealed trait ScheduleCommand extends BotCommand

object ScheduleCommand {
  final case class SelectMonth(month: YearMonth) extends ScheduleCommand
  final case class SelectDate(date: LocalDate) extends ScheduleCommand
  final case class SelectTimePage(page: Int) extends ScheduleCommand
  final case class SelectTime(time: LocalTime) extends ScheduleCommand
  final case class SelectCardOfDay(delay: Duration) extends ScheduleCommand
  object Confirm extends ScheduleCommand
}