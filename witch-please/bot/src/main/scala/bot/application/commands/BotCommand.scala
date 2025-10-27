package bot.application.commands

import java.time.*
import java.util.UUID

sealed trait BotCommand
sealed trait TarotCommand extends BotCommand
sealed trait ScheduleCommand extends BotCommand

object BotCommand {
  case object Noop extends BotCommand
  case object Help extends BotCommand
  case object Unknown extends BotCommand
}

object TarotCommand {
  case object Start extends TarotCommand
  case object CreateProject extends TarotCommand
  final case class SelectProject(projectId: UUID) extends TarotCommand
  case object CreateSpread extends TarotCommand
  final case class SelectSpread(spreadId: UUID, cardCount: Int) extends TarotCommand
  final case class CreateCard(index: Int) extends TarotCommand
  case object PublishSpread extends TarotCommand
}

object ScheduleCommand {
  final case class SelectMonth(month: YearMonth) extends ScheduleCommand
  final case class SelectDate(date: LocalDate) extends ScheduleCommand
  final case class SelectTime(time: LocalTime) extends ScheduleCommand
  case object Confirm extends ScheduleCommand
}

